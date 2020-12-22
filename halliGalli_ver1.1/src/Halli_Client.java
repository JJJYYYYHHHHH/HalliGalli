import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

// ����ȭ�� �����ϴ� Ŭ����
class SetBack extends JPanel {
	ImageIcon icon = new ImageIcon("backG.jpg");
	Image img = icon.getImage();

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0, this);
	}
}

public class Halli_Client extends JFrame implements Runnable, ActionListener {

//	GUI ����
	JLabel 						la_State = new JLabel("<����â>"); 		// ���� ���� ���
	JLabel 						la_PlayerInfo = new JLabel("<�ο�����>"); // �ο�����
	JLabel[] 					player = new JLabel[4]; 				// �÷��̾�_�̸�
	JLabel[] 					cnt_Card = new JLabel[4]; 				// �÷��̾�_����
	JButton						connectBtn = new JButton("����");
	JButton 					readyBtn = new JButton("�غ�");
	JButton 					openBtn = new JButton("ī�������");
	JButton 					bellBtn = new JButton("��ġ��");
	JScrollPane 				sc_Pane; 								// ä��_ScrollPane
	JTextArea 					ta_Info; 								// ä��_Area
	JTextField 					tf_Talk; 								// ä��_�Է��ʵ�
	JTextField 					tf_Id; 									// ����_ID �Է��ʵ�
	JTextField 					tf_Ip; 									// ����_IP �Է��ʵ�
	
//	����� ����Ʈ
	DefaultListModel 			model = new DefaultListModel(); 		// ����Ʈ�� �������� �Ѹ� ����� ���� �޴°�
	JList 						li_Player = new JList(model); 			// ����� ����Ʈ

//	Ư�� ������ ���� boolean
	boolean 					ready = false; 							// ready �Ǵ��ϱ����� boolean
	boolean 					bell_Check = false;						// �� ���� ���и� �Ǵ��ϱ����� boolean
	
//	������ ���� ����
	BufferedReader 				reader; 								// �Է½�Ʈ��
	PrintWriter 				writer; 								// ��½�Ʈ��
	Socket 						socket; 								// ����

//	���� �̸��� �޴� String
	String 						name = null; 							// �÷��̾� �̸�
	
//	ī�忡 ���� ����
	int[] 						cardNum; 								// ��üī���ȣ
	JLabel[] 					player_Card;							// �� �÷��̾��� ī�� �̹���
	Vector<Integer>[] 			playCard; 								// ���� �÷��̾� ī�� ����
	Vector<Integer> 			tableCard; 								// �������� ī�� + ���̺� ī�� ����
	Vector<Integer> 			openCard; 								// �������� ī�� ���� (�˻��ؾߵǴ� ī�� )
	DefaultListModel 			model2 = new DefaultListModel(); 		// ����Ʈ�� �������� �Ѹ� ī������ �޴� ��

//	�̹���
	ImageIcon 			cardBack_img = new ImageIcon("CardBack.jpg");	// ī�� �޸� �̹���
	ImageIcon 			dieIcon_img = new ImageIcon("Die.jpg");			// ���� �÷��̾� �̹���
	ImageIcon 			win = new ImageIcon("Player_Image.jpg");			// �̱� �÷��̾� �̹���
	ImageIcon[] 		banana = new ImageIcon[5];						// �ٳ��� �̹���[5]
	ImageIcon[] 		Lemon = new ImageIcon[5];						// ���� �̹���[5]
	ImageIcon[] 		Peach = new ImageIcon[5];						// ������ �̹���[5]
	ImageIcon[] 		Straw = new ImageIcon[5];						// ���� �̹���[5]

	
	public Halli_Client() {
		setTitle("�Ҹ����� ����");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// ����ȭ�� �����ϴ� �κ�
		SetBack set_img = new SetBack();
		set_img.setOpaque(false);
		setContentPane(set_img);

		// [ [ [ �⺻ ���� ] ] ]
		Container p = getContentPane();
		p.setLayout(null);
		EtchedBorder eborder = new EtchedBorder(EtchedBorder.RAISED); // Ƣ��� ��輱

		// [ [ [ ���� ���� ] ] ]
		la_State.setFont(new Font("Gothic", Font.BOLD | Font.ITALIC, 20));
		la_State.setForeground(Color.BLUE);
		la_State.setBounds(30, 40, 480, 40);

		p.add(la_State);

		// [ [ [ ī�� �̹��� ���� ] ] ]
		// [ ī�� �޸� ���� ]
		player_Card = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			player_Card[i] = new JLabel(cardBack_img);
		}
		JLabel[] cardBack = new JLabel[4];
		for (int i = 0; i < 4; i++) {
			cardBack[i] = new JLabel(cardBack_img);
		}

		player_Card[0].setBounds(50, 110, 100, 150);
		cardBack[0].setBounds(150, 110, 100, 150);

		player_Card[1].setBounds(280, 110, 100, 150);
		cardBack[1].setBounds(380, 110, 100, 150);

		player_Card[2].setBounds(50, 330, 100, 150);
		cardBack[2].setBounds(150, 330, 100, 150);

		player_Card[3].setBounds(280, 330, 100, 150);
		cardBack[3].setBounds(380, 330, 100, 150);

		for (int i = 0; i < 4; i++) {
			p.add(player_Card[i]);
			p.add(cardBack[i]);
		}

		// [ ī�� ��ȣ ���� ]
		cardNum = new int[56]; // �ääääääääää�
		playCard = new Vector[4];
		for (int i = 0; i < 4; i++)
			playCard[i] = new Vector();
		tableCard = new Vector();
		openCard = new Vector();

		// [ ī�� �̹��� ���� ]
		for (int i = 0; i < 5; i++) {
			banana[i] = new ImageIcon("Banana_" + (i + 1) + ".jpg");
			Lemon[i] = new ImageIcon("Lemon_" + (i + 1) + ".jpg");
			Peach[i] = new ImageIcon("Peach_" + (i + 1) + ".jpg");
			Straw[i] = new ImageIcon("Straw_" + (i + 1) + ".jpg");
		}

		// [ [ [ �÷��̾� �̸� ] ] ]
		for (int i = 0; i < 4; i++) {
			player[i] = new JLabel("Player" + (i + 1));
			player[i].setFont(new Font("Gothic", Font.BOLD, 14));
			player[i].setForeground(Color.BLUE);
			player[i].setSize(100, 50);
		}
		player[0].setLocation(50, 70);
		player[1].setLocation(280, 70);
		player[2].setLocation(50, 290);
		player[3].setLocation(280, 290);

		for (int i = 0; i < 4; i++) {
			p.add(player[i]);
		}

		// [ [ [ �÷��̾� ī�� ���� ���� ] ] ]
		for (int i = 0; i < 4; i++) {
			cnt_Card[i] = new JLabel("0��");
			cnt_Card[i].setFont(new Font("Gothic", Font.BOLD, 15));
			cnt_Card[i].setForeground(Color.RED);
			cnt_Card[i].setSize(50, 50);
		}
		cnt_Card[0].setLocation(130, 70);
		cnt_Card[1].setLocation(360, 70);
		cnt_Card[2].setLocation(130, 290);
		cnt_Card[3].setLocation(360, 290);

		for (int i = 0; i < 4; i++) {
			p.add(cnt_Card[i]);
		}

		// [ [ [ IP, ID �Է� ] ] ]
		JLabel fix_IP = new JLabel("�����ּ� : ");
		fix_IP.setFont(new Font("Ghothic", Font.ITALIC, 13));
		fix_IP.setForeground(Color.BLUE);
		fix_IP.setLocation(515, 20);
		fix_IP.setSize(80, 25);

		JLabel fix_ID = new JLabel("�̸� : ");
		fix_ID.setFont(new Font("Ghothic", Font.ITALIC, 13));
		fix_ID.setForeground(Color.BLUE);
		fix_ID.setLocation(540, 50);
		fix_ID.setSize(50, 25);

		p.add(fix_IP);
		p.add(fix_ID);

		// [ [ [ IP, ID ���� ] ] ]
		// +���Ӽ����ϸ� ip.setEditable(false); ���� (���ڿ� ���� �Ұ����ϰ� �ϱ�)
		tf_Ip = new JTextField(20);
		tf_Ip.setFont(new Font("Gothic", Font.ITALIC, 13));
		tf_Ip.setLocation(580, 20);
		tf_Ip.setSize(150, 25);

		tf_Id = new JTextField(20);
		tf_Id.setFont(new Font("Gothic", Font.ITALIC, 13));
		tf_Id.setLocation(580, 50);
		tf_Id.setSize(150, 25);

		p.add(tf_Ip);
		p.add(tf_Id);

		// [ [ [ ���� ��ư ] ] ]
		connectBtn.setLocation(520, 80);
		connectBtn.setSize(100, 20);

		p.add(connectBtn);

		// [ [ [ �غ� ��ư ] ] ]
		readyBtn.setLocation(630, 80);
		readyBtn.setSize(100, 20);
		readyBtn.setEnabled(false); // ��Ȱ��ȭ

		p.add(readyBtn);

// [ [ [ [ usr_Panel ] ] ] ]
		JPanel usr_Panel = new JPanel();
		usr_Panel.setLayout(new BorderLayout());
		usr_Panel.setLocation(530, 110);
		usr_Panel.setSize(200, 185);
		usr_Panel.setBorder(eborder);

		JPanel usr_Panel_Btn = new JPanel();

		usr_Panel.add(la_PlayerInfo, BorderLayout.NORTH);
		usr_Panel.add(li_Player, BorderLayout.CENTER);

		openBtn.setEnabled(false); // ��Ȱ��ȭ
		bellBtn.setEnabled(false); // ��Ȱ��ȭ

		usr_Panel_Btn.add(openBtn);
		usr_Panel_Btn.add(bellBtn);

		usr_Panel.add(usr_Panel_Btn, BorderLayout.SOUTH);
		p.add(usr_Panel);

// [ [ [ [ text_Panel ] ] ] ]
		JPanel text_Panel = new JPanel();
		text_Panel.setLayout(new BorderLayout());
		text_Panel.setLocation(530, 300);
		text_Panel.setSize(200, 240);
		text_Panel.setBorder(eborder); // ��輱

		ta_Info = new JTextArea("< Well Come!! >\n", 1, 1);
		sc_Pane = new JScrollPane(ta_Info);
		ta_Info.setEditable(false);
		tf_Talk = new JTextField("");
		tf_Talk.setFont(new Font("Gothic", Font.BOLD, 13));
		tf_Talk.setSize(150, 25);

		text_Panel.add(sc_Pane, BorderLayout.CENTER);
		text_Panel.add(tf_Talk, BorderLayout.SOUTH);

		p.add(text_Panel);

		tf_Talk.addActionListener(this);
		connectBtn.addActionListener(this);
		readyBtn.addActionListener(this);
		openBtn.addActionListener(this);
		bellBtn.addActionListener(this);

		setSize(800, 600);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tf_Talk) {				// tf_Talk�� ���� �Է����� ���
			String msg = tf_Talk.getText();
			if (msg.length() == 0) {				// �׳� ���� ģ ���
				return;
			} 
			if (msg.length() >= 30) {				// ���� �ִ� ���
				msg = msg.substring(0, 30);
			}
			writer.println("[MSG]" + msg);
			tf_Talk.setText(""); 					// ���� �� �ʱ�ȭ
		} else if (e.getSource() == connectBtn) {	// ���� ��ư �׼�
			name = tf_Id.getText().trim();
			if (name.length() <= 2 || name.length() > 10) {
				la_State.setText("���̵�� 3~9�� ��� �����մϴ�.");
				tf_Id.requestFocus();
				return;
			}
			connect();
			tf_Id.setText(name);
			tf_Id.setEditable(false);
			tf_Ip.setEditable(false);
			la_State.setText("���Ӽ���!");
			setTitle(name + "���� �Ҹ����� ����");
			writer.println("[CONNECT]" + name);
			connectBtn.setEnabled(false);
			readyBtn.setEnabled(true);
			openBtn.setEnabled(false);
		} else if (e.getSource() == readyBtn) {			// �غ� ��ư �׼�
			if (!ready) {
				ready = true;
				writer.println("[READY]");
				readyBtn.setText("�غ�����");
				la_State.setText("�غ�Ϸ�!");
			} else {
				ready = false;
				writer.println("[NOT READY]");
				readyBtn.setText("�غ�");
				la_State.setText("�غ�����..");
			}
		} else if (e.getSource() == openBtn) {			// ī������� ��ư �׼�
			int pl = 0;
			la_State.setText("����� ī�带 �������ϴ�.");
			writer.println("[TURN]" + pl + "|" + name);
			pl++;
			openBtn.setEnabled(false);

		} else if (e.getSource() == bellBtn) {
			bell_Check();
			writer.println("[BELL]" + name);
		}
	}

// �ο� ���� ����
	void playersInfo() {
		int count = model.getSize();
		la_PlayerInfo.setText("���� " + count + "������");
	}

// �������� ���� �÷��̾� ����Ʈ�� ����Ʈ�� ����.
	void nameList(String msg)
	{
		model.removeAllElements();
		StringTokenizer st = new StringTokenizer(msg, "\t");
		while (st.hasMoreElements()) {
			model.addElement(st.nextToken());
		}
		for (int i = 0; i < model.size(); i++) {
			player[i].setText((String) model.getElementAt(i));
		}
		playersInfo();
	}

// �������� �Ѹ� �ʱ� ī�� ���� ����Ʈ�𵨿� �ֱ�
	void cardList(String msg) 
	{

		model2.removeAllElements();
		StringTokenizer st = new StringTokenizer(msg, "]|["); 	// �������ڷ� ]|[ �� ����ؼ� [] �̰� ������ ���ڶ� ��ǥ(,) �� ����
		while (st.hasMoreElements()) 	// �������� ������ ���� ����
		{
			model2.addElement(st.nextToken());
		}
		System.out.println("�������� �Ѿ�� ī�� ���� :" + msg);
		cardsetting(model2);
	}
	
// playCard ���Ϳ� �������� �� ī������ �ֱ�
	void cardsetting(DefaultListModel model2) { 
		String playcard2_0, playcard2_1, playcard2_2, playcard2_3;
		String a[], b[], c[], d[];

		playcard2_0 = (String) model2.get(0);	// ���ڿ��� ����
		playcard2_1 = (String) model2.get(1);
		playcard2_2 = (String) model2.get(2);
		playcard2_3 = (String) model2.get(3);

		a = playcard2_0.split(", "); 			// ��ǥ�� ������� �� ���ڷ� a �迭�� ���� -> a[14] ����
		b = playcard2_1.split(", ");
		c = playcard2_2.split(", ");
		d = playcard2_3.split(", ");

		System.out.print("a[]: ");
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + ",");
		}
		System.out.print("\n" + "b[]: ");
		for (int i = 0; i < a.length; i++) {
			System.out.print(b[i] + ",");
		}
		System.out.print("\n" + "c[]: ");
		for (int i = 0; i < a.length; i++) {
			System.out.print(c[i] + ",");
		}
		System.out.print("\n" + "d[]: ");
		for (int i = 0; i < a.length; i++) {
			System.out.print(d[i] + ",");
		}

		for (int j = 0; j < a.length; j++) {
			playCard[0].add(Integer.parseInt(a[j])); // �迭�� ���� ���Ϳ� ����
		}

		for (int j = 0; j < a.length; j++) {
			playCard[1].add(Integer.parseInt(b[j])); // �迭�� ���� ���Ϳ� ����
		}

		for (int j = 0; j < a.length; j++) {
			playCard[2].add(Integer.parseInt(c[j])); // �迭�� ���� ���Ϳ� ����
		}

		for (int j = 0; j < a.length; j++) {
			playCard[3].add(Integer.parseInt(d[j])); // �迭�� ���� ���Ϳ� ����
		}
		System.out.println(" ");

		for (int i = 0; i < 4; i++) { 	// Ȯ���� ���
			for (int j = 0; j < 14; j++) {
				System.out.print(playCard[i].get(j) + " ");
			}
			System.out.println();
		}
		CntCardSet(); 	// ��� Label ����
	}

	@Override
	public void run() {
		String msg;
		try {
			while ((msg = reader.readLine()) != null) {
		// �÷��̾��Ʈ�� �޴´�.
				if (msg.startsWith("[PLAYERS]")) 
				{
					nameList(msg.substring(9));
				}
		// ���� ������ ���
				else if (msg.startsWith("[ENTER]")) 
				{
					model.addElement(msg.substring(7));
					playersInfo();
					ta_Info.append("[" + msg.substring(7) + "]���� �����Ͽ����ϴ�.\n");
					sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
					validate();
				}
		// ������ ���������
				else if (msg.startsWith("[DISCONNECT]")) 
				{
					model.removeElement(msg.substring(6));
					playersInfo();
					ta_Info.append("[" + msg.substring(12) + "]���� �������ϴ�.\n");
					sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
					validate();
				}
		// ������ ���۵� ���
				else if (msg.startsWith("������ �����մϴ�.")) 
				{
					openBtn.setEnabled(false);
					bellBtn.setEnabled(true);
					readyBtn.setEnabled(false);
				}
		// ī��й�
				else if (msg.startsWith("[ī��й�]")) {
					cardList(msg.substring(7));
				}
		// openBtn Ȱ��ȭ
				else if (msg.startsWith("[������]")) {
					openBtn.setEnabled(true);
				}
		// �������� ī������� �� �ٽ� �׸��� ��û
				else if (msg.startsWith("[REPAINT]")) 
				{
					int a = msg.indexOf("|");
					int b = Integer.parseInt(msg.substring(a + 1));
					FlipCard(b);
				}
		// ī�尡 ��� �׾��� ��� �޴� �޼���
				else if (msg.startsWith("[DEAD]")) 
				{
					la_State.setText("����� �׾����ϴ�.");
					la_State.setForeground(Color.RED);
					openBtn.setEnabled(false);
					bellBtn.setEnabled(false);
				}
		// ������ ������ ���
				else if (msg.startsWith("[END]")) {
					openBtn.setEnabled(false);
					bellBtn.setEnabled(false);
				}
		// �� �︮��
				
				else if (msg.startsWith("[BELL]")) {
					int pl = Integer.parseInt(msg.substring(6, 7));
					String name = msg.substring(7);
					BellPressCheck(pl, name);
				}
		// �� ��� ��Ȱ��ȭ, ���� ǥ��
				else if (msg.startsWith("[STOPBELL]")) {
					bellBtn.setEnabled(false);
					if (bell_Check) {
						try {
							Thread.sleep(1000);
							la_State.setText("����!");
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.getMessage();
						}
					} else {
						try {
							Thread.sleep(1000);
							la_State.setText("����!");
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.getMessage();
						}
					}
				}
		// �ٽ� �� Ȱ��ȭ
				else if (msg.startsWith("[GOBELL]")) {
					bellBtn.setEnabled(true);
				}
		// la_State�� �� �޽��� ���
				else if (msg.startsWith("[SETLA]")) {
					la_State.setText(msg.substring(7));
				}
		// 
				else if (msg.startsWith("[DEADLOCK]")) {
					int a = Integer.parseInt(msg.substring(10));
					cnt_Card[a].setText("0��");;
				}
		// ä��â�� �޽����� �Է��� ���
				else // �׳� �޼����� ������� �׳� ���
				{
					ta_Info.append(msg + "\n");
					sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
					validate();
				}
			}
		} catch (IOException ie) {
			ta_Info.append(ie + "\n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
		}
		ta_Info.append("������ ������ϴ�.\n");
		sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
		validate();
	}

// ���� ����
	void connect() {
		try {
			String ip = tf_Ip.getText();
			ta_Info.append("������ ������ ��û�մϴ�.\n");
			socket = new Socket(ip, 7777);
			ta_Info.append("--���� ����--\n");
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
		} catch (IOException e) {
			ta_Info.append(e + "\n\n���� ����..\n");
		}
	}
	
// ī�� ������ �ÿ� ��ɵ� . ���ڰ˻��ؼ� �´� ī�� ����ְ�, �÷���ī��迭 ����, ��ī�� �ӽ�����
	void FlipCard(int pl) { 
		int sc;
		int temp;
		
		DieCheck(pl, name); // ��������
		
		if(playCard[pl].size() > 0) { //��������
		if (playCard[pl].firstElement() < 14) { // �ٳ�����?
			if (playCard[pl].firstElement() < 5)// 1����?
				player_Card[pl].setIcon(banana[0]);
			else if (playCard[pl].firstElement() > 4 && playCard[pl].firstElement() < 8)// 2����?
				player_Card[pl].setIcon(banana[1]);
			else if (playCard[pl].firstElement() > 7 && playCard[pl].firstElement() < 11)// 3����?
				player_Card[pl].setIcon(banana[2]);
			else if (playCard[pl].firstElement() > 10 && playCard[pl].firstElement() < 13)// 4����?
				player_Card[pl].setIcon(banana[3]);
			else
				player_Card[pl].setIcon(banana[4]);
		}
		else if (playCard[pl].firstElement() >= 14 && playCard[pl].firstElement() < 28) { // �����̳�?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1����?
				player_Card[pl].setIcon(Lemon[0]);
			else if (sc > 4 && sc < 8)// 2����?
				player_Card[pl].setIcon(Lemon[1]);
			else if (sc > 7 && sc < 11)// 3����?
				player_Card[pl].setIcon(Lemon[2]);
			else if (sc > 10 && sc < 13)// 4����?
				player_Card[pl].setIcon(Lemon[3]);
			else
				player_Card[pl].setIcon(Lemon[4]);
		}
		else if (playCard[pl].firstElement() >= 28 && playCard[pl].firstElement() < 42) { // �����Ƴ�?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1����?
				player_Card[pl].setIcon(Peach[0]);
			else if (sc > 4 && sc < 8)// 2����?
				player_Card[pl].setIcon(Peach[1]);
			else if (sc > 7 && sc < 11)// 3����?
				player_Card[pl].setIcon(Peach[2]);
			else if (sc > 10 && sc < 13)// 4����?
				player_Card[pl].setIcon(Peach[3]);
			else
				player_Card[pl].setIcon(Peach[4]);
		}
		else if (playCard[pl].firstElement() >= 42 && playCard[pl].firstElement() < 56) { // �����?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1����?
				player_Card[pl].setIcon(Straw[0]);
			else if (sc > 4 && sc < 8)// 2����?
				player_Card[pl].setIcon(Straw[1]);
			else if (sc > 7 && sc < 11)// 3����?
				player_Card[pl].setIcon(Straw[2]);
			else if (sc > 10 && sc < 13)// 4����?
				player_Card[pl].setIcon(Straw[3]);
			else
				player_Card[pl].setIcon(Straw[4]);
		}

		temp = playCard[pl].firstElement(); // ���徿 � ���̺�(tableCard����)�� ��� ���
		
		if (openCard.size() == 4) { 		// ���� �� ī�尡 4���� �� �˻� �ؾߵ� ī�� ����
			openCard.remove(0);
			openCard.add(temp);
		} else {
			openCard.add(temp);
		}

		tableCard.add(temp);
		playCard[pl].remove(0);
		if(playCard[pl].size() == 1) {
			playCard[pl].clear();
		}
		} //��������
		CntCardSet();

	}

// �� ������ �� �˻��ؼ� ���ó��
	void BellPressCheck(int pl, String name) {
		int banana = 0;
		int lemon = 0;
		int peach = 0;
		int straw = 0;
		int sc;
		int openCardSize;
		int playerNum = 0;

		for (int i = 0; i < 4; i++) {
			if (playCard[i].size() > 0)
				playerNum = playerNum + 1;
		}

		openCardSize = openCard.size();
		if (openCardSize > 3) { // ���̺� ī�� 4�� �̻��� ��
			for (int i = openCard.size() - 1; i > openCard.size() - playerNum - 1; i--) { // ī�尡 ������ ����� �˻��ؼ� �� ������ ���.
				if (openCard.get(i) < 14) { // �ٳ�����?
					if (openCard.get(i) < 5)
						banana = banana + 1;
					else if (openCard.get(i) > 4 && openCard.get(i) < 8)
						banana = banana + 2;
					else if (openCard.get(i) > 7 && openCard.get(i) < 11)
						banana = banana + 3;
					else if (openCard.get(i) > 10 && openCard.get(i) < 13)
						banana = banana + 4;
					else
						banana = banana + 5;
				}
				else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						lemon = lemon + 1;
					else if (sc > 4 && sc < 8)
						lemon = lemon + 2;
					else if (sc > 7 && sc < 11)
						lemon = lemon + 3;
					else if (sc > 10 && sc < 13)
						lemon = lemon + 4;
					else
						lemon = lemon + 5;
				}
				else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // �����Ƴ�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						peach = peach + 1;
					else if (sc > 4 && sc < 8)
						peach = peach + 2;
					else if (sc > 7 && sc < 11)
						peach = peach + 3;
					else if (sc > 10 && sc < 13)
						peach = peach + 4;
					else
						peach = peach + 5;
				}
				else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						straw = straw + 1;
					else if (sc > 4 && sc < 8)
						straw = straw + 2;
					else if (sc > 7 && sc < 11)
						straw = straw + 3;
					else if (sc > 10 && sc < 13)
						straw = straw + 4;
					else
						straw = straw + 5;
				}
			}
		}
		else {// ���̺� ī�尡 4�� �̸��� ��.
			for (int i = openCard.size() - 1; i >= 0; i--) { // ī�尡 ������ ����� �˻��ؼ� �� ������ ���.
				if (openCard.get(i) < 14) { // �ٳ�����?
					if (openCard.get(i) < 5)
						banana = banana + 1;
					else if (openCard.get(i) > 4 && openCard.get(i) < 8)
						banana = banana + 2;
					else if (openCard.get(i) > 7 && openCard.get(i) < 11)
						banana = banana + 3;
					else if (openCard.get(i) > 10 && openCard.get(i) < 13)
						banana = banana + 4;
					else
						banana = banana + 5;
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						lemon = lemon + 1;
					else if (sc > 4 && sc < 8)
						lemon = lemon + 2;
					else if (sc > 7 && sc < 11)
						lemon = lemon + 3;
					else if (sc > 10 && sc < 13)
						lemon = lemon + 4;
					else
						lemon = lemon + 5;
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // �����Ƴ�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						peach = peach + 1;
					else if (sc > 4 && sc < 8)
						peach = peach + 2;
					else if (sc > 7 && sc < 11)
						peach = peach + 3;
					else if (sc > 10 && sc < 13)
						peach = peach + 4;
					else
						peach = peach + 5;
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						straw = straw + 1;
					else if (sc > 4 && sc < 8)
						straw = straw + 2;
					else if (sc > 7 && sc < 11)
						straw = straw + 3;
					else if (sc > 10 && sc < 13)
						straw = straw + 4;
					else
						straw = straw + 5;
				}
			}
		} // �� ���Ϻ����鿡 ��� �Ϸ�.
	//�� ����
		if (banana == 5 || lemon == 5 || peach == 5 || straw == 5) {
			bell_Check = true;
			for (int i = 0; i < tableCard.size(); i++)
				playCard[pl].add(tableCard.get(i));
			ta_Info.append(name + "���� �� �︮�⿡ ���� .\n" + "�������� ī�带 �������ϴ�.\n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			tableCard.removeAllElements();		// �������� ���̺� ī�� �� �ְ� ���̺� �ʱ�ȭ
			openCard.removeAllElements(); 		// �������� ī��� �ʱ�ȭ
			CardBackSet();

		} 
	// �� ����
		else { 
			ta_Info.append(name + "���� ��ġ�⿡ �����߽��ϴ�.\n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			if (playCard[pl].size() == 0) {
				DieCheck(pl, name);
			} 
			else if (playCard[pl].size() < 3) { // ����ó�� 3�� �̸��� ��. ���̺� ����ī�� ���
				for (int i = 0; i < playCard[pl].size(); i++)
					tableCard.add(playCard[pl].get(i));
				playCard[pl].removeAllElements();
				DieCheck(pl, name);
			} 
			else {
				for (int i = 0; i < 3; i++) {
					tableCard.add(0, playCard[pl].get(0));
					playCard[pl].remove(0);
				}
			}
		}

		System.out.println("---------------------------"); // Ȯ���� ��±���
		for (int i = 0; i < 4; i++) {
			System.out.print("�÷��̾�" + i + " ");
			for (int j = 0; j < playCard[i].size(); j++) {
				System.out.print(+playCard[i].get(j) + " ");
			}
			System.out.println();
		}
		System.out.print("�ǿ� �� ī�� ");
		for (int i = 0; i < tableCard.size(); i++) {
			System.out.print(tableCard.get(i) + " ");
		}
		System.out.println();
		System.out.print("�������� ī�� ");
		System.out.println(" �ٳ���" + banana + " ����" + lemon + " ������" + peach + " ����" + straw);
		System.out.println("---------------------------");

		CntCardSet();
	}
	
// �������üũ. �׾����� 1 ������� 0 ����.
	int DieCheck(int pl, String name) { 
		int dieFlag = 0;
		int firstFlag = 0;

		if (playCard[pl].size() == 0) { // ���� ī����� 0���̳�?
			player_Card[pl].setIcon(dieIcon_img);
			dieFlag = 1;
			System.out.println("�÷��̾�" + pl + " ���");
			ta_Info.append("�Ѹ��� �÷��̾ ����Ͽ����ϴ�. \n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			writer.println("[DEAD]" + pl);
			cnt_Card[pl].setText("0��");
		}

		for (int i = 0; i < 4; i++) {
			if (playCard[i].size() > 0)
				firstFlag = firstFlag + 1;
		}

		if (firstFlag == 1) { // 1�� ����
			for (int i = 0; i < 4; i++) {
				if (playCard[i].size() > 0) {
					player_Card[i].setIcon(win);
					openBtn.setEnabled(false);
					writer.println("[WIN]" + i);
				}
			}
		}
		CntCardSet();
		return dieFlag;
	}
	
// ī�� �޸� �������ִ� �Լ�
	void CardBackSet() { 
		for (int i = 0; i < 4; i++) {
			player_Card[i].setIcon(cardBack_img);
			if (playCard[i].size() == 0)
				player_Card[i].setIcon(dieIcon_img);
		}
	}
	
// ��� ���̺� ����
	void CntCardSet() { 
		String[] playcard = new String[4];
		String a[] = null, b[] = null, c[] = null, d[] = null;

		for (int i = 0; i < 4; i++) {
			playcard[i] = playCard[i].toString(); // ���ڿ��� ����
		}
		a = playcard[0].split(", "); // ��ǥ�� ������� �� ���ڷ� a �迭�� ���� -> a[14] ����
		b = playcard[1].split(", ");
		c = playcard[2].split(", ");
		d = playcard[3].split(", ");

		cnt_Card[0].setText(a.length + "��");
		cnt_Card[1].setText(b.length + "��");
		cnt_Card[2].setText(c.length + "��");
		cnt_Card[3].setText(d.length + "��");
	}
	
// �� �︮�� ���� ���� üũ�ϴ� �޼ҵ�
	void bell_Check() { 

		int banana = 0;
		int lemon = 0;
		int peach = 0;
		int straw = 0;
		int sc;
		int openCardSize;
		int playerNum = 0;

		for (int i = 0; i < 4; i++) {
			if (playCard[i].size() > 0)
				playerNum = playerNum + 1;
		}

		openCardSize = openCard.size();
		if (openCardSize > 3) { // ���̺� ī�� 4�� �̻��� ��
			for (int i = openCard.size() - 1; i > openCard.size() - playerNum - 1; i--) { // ī�尡 ������ ����� �˻��ؼ� �� ������ ���.
				if (openCard.get(i) < 14) { // �ٳ�����?
					if (openCard.get(i) < 5)
						banana = banana + 1;
					else if (openCard.get(i) > 4 && openCard.get(i) < 8)
						banana = banana + 2;
					else if (openCard.get(i) > 7 && openCard.get(i) < 11)
						banana = banana + 3;
					else if (openCard.get(i) > 10 && openCard.get(i) < 13)
						banana = banana + 4;
					else
						banana = banana + 5;
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						lemon = lemon + 1;
					else if (sc > 4 && sc < 8)
						lemon = lemon + 2;
					else if (sc > 7 && sc < 11)
						lemon = lemon + 3;
					else if (sc > 10 && sc < 13)
						lemon = lemon + 4;
					else
						lemon = lemon + 5;
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // �����Ƴ�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						peach = peach + 1;
					else if (sc > 4 && sc < 8)
						peach = peach + 2;
					else if (sc > 7 && sc < 11)
						peach = peach + 3;
					else if (sc > 10 && sc < 13)
						peach = peach + 4;
					else
						peach = peach + 5;
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						straw = straw + 1;
					else if (sc > 4 && sc < 8)
						straw = straw + 2;
					else if (sc > 7 && sc < 11)
						straw = straw + 3;
					else if (sc > 10 && sc < 13)
						straw = straw + 4;
					else
						straw = straw + 5;
				}
			}
		} else {// ���̺� ī�尡 4�� �̸��� ��.
			for (int i = openCard.size() - 1; i >= 0; i--) { // ī�尡 ������ ����� �˻��ؼ� �� ������ ���.
				if (openCard.get(i) < 14) { // �ٳ�����?
					if (openCard.get(i) < 5)
						banana = banana + 1;
					else if (openCard.get(i) > 4 && openCard.get(i) < 8)
						banana = banana + 2;
					else if (openCard.get(i) > 7 && openCard.get(i) < 11)
						banana = banana + 3;
					else if (openCard.get(i) > 10 && openCard.get(i) < 13)
						banana = banana + 4;
					else
						banana = banana + 5;
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						lemon = lemon + 1;
					else if (sc > 4 && sc < 8)
						lemon = lemon + 2;
					else if (sc > 7 && sc < 11)
						lemon = lemon + 3;
					else if (sc > 10 && sc < 13)
						lemon = lemon + 4;
					else
						lemon = lemon + 5;
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // �����Ƴ�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						peach = peach + 1;
					else if (sc > 4 && sc < 8)
						peach = peach + 2;
					else if (sc > 7 && sc < 11)
						peach = peach + 3;
					else if (sc > 10 && sc < 13)
						peach = peach + 4;
					else
						peach = peach + 5;
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // �����̳�?
					sc = openCard.get(i) % 14;
					if (sc < 5)
						straw = straw + 1;
					else if (sc > 4 && sc < 8)
						straw = straw + 2;
					else if (sc > 7 && sc < 11)
						straw = straw + 3;
					else if (sc > 10 && sc < 13)
						straw = straw + 4;
					else
						straw = straw + 5;
				}
			}
		} // �� ���Ϻ����鿡 ��� �Ϸ�.
		if (banana == 5 || lemon == 5 || peach == 5 || straw == 5) // �� ���Ƴ�?
			bell_Check = true;
		else
			bell_Check = false;
	}

	public static void main(String[] args) {
		new Halli_Client();
	}
}