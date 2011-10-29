import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class scroll_test {
    
	public class myPanel extends JPanel implements Scrollable, ChangeListener {
		private JViewport viewport;
		
		@Override
		public void paint(Graphics g) {
			Rectangle visible = viewport.getViewRect();
			Dimension size = visible.getSize();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.getSize().width, this.getSize().height);
			g.setColor(Color.RED);
			g.fillRect(visible.x, visible.y, size.width, size.height / 2);
			g.setColor(Color.GREEN);
			g.fillRect(visible.x, visible.y + size.height / 2, size.width, size.height - size.height / 2);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(800,800);
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle arg0, int arg1,
				int arg2) {
			return 1;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
			return 1;
		}
				
		@Override
		public void stateChanged(ChangeEvent o) {
			repaint();
		}
		
		public void setViewport(JViewport p) {
			viewport = p;
		}
	}
	
    JFrame frame;
	JScrollPane scrollPane;
	myPanel innerPanel;
	
    public void run() {
	    frame = new JFrame("Scroll test");
	    JPanel contentPane = new JPanel();
		innerPanel = new myPanel();
		scrollPane = new JScrollPane(innerPanel);
		scrollPane.setPreferredSize(new Dimension(400,400));
		scrollPane.getViewport().addChangeListener(innerPanel);
		innerPanel.setViewport(scrollPane.getViewport());
		
		contentPane.setLayout(new GridLayout(1, 1));
		
		contentPane.add(scrollPane);
		frame.setContentPane(contentPane);
		
	    	    
	    frame.pack();
	    frame.setVisible(true);
    }
	
    public static void main(String[] args) {
		scroll_test test = new scroll_test();
		test.run();
    }
}