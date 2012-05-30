package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MainWnd extends JFrame implements ActionListener {

	private JButton butt = new JButton("Wybierz plik..");
	private JLabel desc = new JLabel(
			"Program rozpoznaje cyfry zapisane w plikach wav.");
	final JFileChooser fc = new JFileChooser();
	
	public MainWnd() {
		butt.addActionListener(this);
		
		setLayout(new GridLayout(2, 1));
		add(desc);
		add(butt);
		setSize(400, 100);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		fc.showOpenDialog(this);
		File f = fc.getSelectedFile();
		int numb = NNApp.testNetwork(f);
		desc.setText("Plik '" + f.getName() + "' - rozpoznana cyfra: " + String.valueOf(numb));
	}
}
