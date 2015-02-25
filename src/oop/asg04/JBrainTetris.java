package oop.asg04;

import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

public class JBrainTetris extends JTetris {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Default brain
	private DefaultBrain brain;
	private Brain.Move bestMove;
	private JCheckBox brainMode;
	private int countPiece;

	// Adversary
	private JSlider adversary;
	private Brain.Move worseMove;

	// OKStatus
	private JLabel statusOk;

	JBrainTetris(int pixels) {
		super(pixels);
		brain = new DefaultBrain();
		bestMove = new Brain.Move();
		countPiece = 0;
	}

	public JComponent createControlPanel() {
		JPanel panel = (JPanel) super.createControlPanel();

		// Default brain
		panel.add(new JLabel("Brain:"));
		brainMode = new JCheckBox("Brain active");
		panel.add(brainMode);

		// Adversary
		JPanel panelSlider = new JPanel();
		panelSlider.add(new JLabel("Adversary"));
		adversary = new JSlider(0, 100, 0);
		adversary.setPreferredSize(new Dimension(100, 15));
		panelSlider.setMaximumSize(new Dimension(400, 100));
		panelSlider.add(adversary);
		panel.add(panelSlider);

		// OKStatus
		JPanel panelOk = new JPanel();
		statusOk = new JLabel("OK");
		panelOk.add(statusOk);
		panel.add(panelOk);

		return panel;
	}

	public void tick(int verb) {
		boolean ticked = brainMode.isSelected();
		if (verb == DOWN && ticked == true) {
			if ((countPiece - count) != 0) {
				board.undo();
				bestMove = brain
						.bestMove(board, currentPiece, HEIGHT, bestMove);
				countPiece = count;
			}
			if (bestMove == null) {
			} else {
				if (currentPiece.equals(bestMove.piece)) {
				} else {
					currentPiece = currentPiece.fastRotation();
				}
				if ((bestMove.x - currentX) < 0)
					super.tick(LEFT);
				else if ((bestMove.x - currentX) > 0)
					super.tick(RIGHT);
			}
		}
		super.tick(verb);
	}

	public Piece pickNextPiece() {
		int difficultLevel = random.nextInt(100);
		int adversaryLevel = adversary.getValue();
		if ((difficultLevel - adversaryLevel) < 0) {
			double worseScore = 0;
			statusOk.setText("*OK*");
			Piece worsePiece = null;
			int i = 0;
			while (i < pieces.length) {
				worseMove = brain.bestMove(board, pieces[i], HEIGHT, worseMove);
				if (worseMove != null) {
					if ((worseScore - worseMove.score) >= 0) {
					} else {
						worsePiece = pieces[i];
						worseScore = worseMove.score;
					}
				} else {
					return super.pickNextPiece();
				}
				i++;
			}
			return worsePiece;
		} else {
			statusOk.setText("OK");
			return super.pickNextPiece();
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
		}

		JBrainTetris tetris = new JBrainTetris(16);
		JFrame frame = JTetris.createFrame(tetris);
		frame.setVisible(true);
	}

}