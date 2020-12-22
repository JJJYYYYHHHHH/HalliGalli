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

// 바탕화면 세팅하는 클래스
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

//	GUI 구성
	JLabel 						la_State = new JLabel("<정보창>"); 		// 게임 정보 출력
	JLabel 						la_PlayerInfo = new JLabel("<인원정보>"); // 인원정보
	JLabel[] 					player = new JLabel[4]; 				// 플레이어_이름
	JLabel[] 					cnt_Card = new JLabel[4]; 				// 플레이어_수량
	JButton						connectBtn = new JButton("접속");
	JButton 					readyBtn = new JButton("준비");
	JButton 					openBtn = new JButton("카드뒤집기");
	JButton 					bellBtn = new JButton("종치기");
	JScrollPane 				sc_Pane; 								// 채팅_ScrollPane
	JTextArea 					ta_Info; 								// 채팅_Area
	JTextField 					tf_Talk; 								// 채팅_입력필드
	JTextField 					tf_Id; 									// 접속_ID 입력필드
	JTextField 					tf_Ip; 									// 접속_IP 입력필드
	
//	사용자 리스트
	DefaultListModel 			model = new DefaultListModel(); 		// 리스트모델 서버에서 뿌린 사용자 정보 받는곳
	JList 						li_Player = new JList(model); 			// 사용자 리스트

//	특정 동작을 위한 boolean
	boolean 					ready = false; 							// ready 판단하기위한 boolean
	boolean 					bell_Check = false;						// 벨 성공 실패를 판단하기위한 boolean
	
//	소켓을 위한 정보
	BufferedReader 				reader; 								// 입력스트림
	PrintWriter 				writer; 								// 출력스트림
	Socket 						socket; 								// 소켓

//	최초 이름을 받는 String
	String 						name = null; 							// 플레이어 이름
	
//	카드에 대한 정보
	int[] 						cardNum; 								// 전체카드번호
	JLabel[] 					player_Card;							// 각 플레이어의 카드 이미지
	Vector<Integer>[] 			playCard; 								// 현재 플레이어 카드 저장
	Vector<Integer> 			tableCard; 								// 뒤집어진 카드 + 테이블 카드 저장
	Vector<Integer> 			openCard; 								// 뒤집어진 카드 저장 (검사해야되는 카드 )
	DefaultListModel 			model2 = new DefaultListModel(); 		// 리스트모델 서버에서 뿌린 카드정보 받는 곳

//	이미지
	ImageIcon 			cardBack_img = new ImageIcon("CardBack.jpg");	// 카드 뒷면 이미지
	ImageIcon 			dieIcon_img = new ImageIcon("Die.jpg");			// 죽은 플레이어 이미지
	ImageIcon 			win = new ImageIcon("Player_Image.jpg");			// 이긴 플레이어 이미지
	ImageIcon[] 		banana = new ImageIcon[5];						// 바나나 이미지[5]
	ImageIcon[] 		Lemon = new ImageIcon[5];						// 레몬 이미지[5]
	ImageIcon[] 		Peach = new ImageIcon[5];						// 복숭아 이미지[5]
	ImageIcon[] 		Straw = new ImageIcon[5];						// 딸기 이미지[5]

	
	public Halli_Client() {
		setTitle("할리갈리 게임");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 바탕화면 세팅하는 부분
		SetBack set_img = new SetBack();
		set_img.setOpaque(false);
		setContentPane(set_img);

		// [ [ [ 기본 세팅 ] ] ]
		Container p = getContentPane();
		p.setLayout(null);
		EtchedBorder eborder = new EtchedBorder(EtchedBorder.RAISED); // 튀어나온 경계선

		// [ [ [ 정보 세팅 ] ] ]
		la_State.setFont(new Font("Gothic", Font.BOLD | Font.ITALIC, 20));
		la_State.setForeground(Color.BLUE);
		la_State.setBounds(30, 40, 480, 40);

		p.add(la_State);

		// [ [ [ 카드 이미지 셋팅 ] ] ]
		// [ 카드 뒷면 셋팅 ]
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

		// [ 카드 번호 셋팅 ]
		cardNum = new int[56]; // ㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓㅓ
		playCard = new Vector[4];
		for (int i = 0; i < 4; i++)
			playCard[i] = new Vector();
		tableCard = new Vector();
		openCard = new Vector();

		// [ 카드 이미지 셋팅 ]
		for (int i = 0; i < 5; i++) {
			banana[i] = new ImageIcon("Banana_" + (i + 1) + ".jpg");
			Lemon[i] = new ImageIcon("Lemon_" + (i + 1) + ".jpg");
			Peach[i] = new ImageIcon("Peach_" + (i + 1) + ".jpg");
			Straw[i] = new ImageIcon("Straw_" + (i + 1) + ".jpg");
		}

		// [ [ [ 플레이어 이름 ] ] ]
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

		// [ [ [ 플레이어 카드 보유 수량 ] ] ]
		for (int i = 0; i < 4; i++) {
			cnt_Card[i] = new JLabel("0장");
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

		// [ [ [ IP, ID 입력 ] ] ]
		JLabel fix_IP = new JLabel("서버주소 : ");
		fix_IP.setFont(new Font("Ghothic", Font.ITALIC, 13));
		fix_IP.setForeground(Color.BLUE);
		fix_IP.setLocation(515, 20);
		fix_IP.setSize(80, 25);

		JLabel fix_ID = new JLabel("이름 : ");
		fix_ID.setFont(new Font("Ghothic", Font.ITALIC, 13));
		fix_ID.setForeground(Color.BLUE);
		fix_ID.setLocation(540, 50);
		fix_ID.setSize(50, 25);

		p.add(fix_IP);
		p.add(fix_ID);

		// [ [ [ IP, ID 셋팅 ] ] ]
		// +접속성공하면 ip.setEditable(false); 실행 (문자열 편집 불가능하게 하기)
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

		// [ [ [ 접속 버튼 ] ] ]
		connectBtn.setLocation(520, 80);
		connectBtn.setSize(100, 20);

		p.add(connectBtn);

		// [ [ [ 준비 버튼 ] ] ]
		readyBtn.setLocation(630, 80);
		readyBtn.setSize(100, 20);
		readyBtn.setEnabled(false); // 비활성화

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

		openBtn.setEnabled(false); // 비활성화
		bellBtn.setEnabled(false); // 비활성화

		usr_Panel_Btn.add(openBtn);
		usr_Panel_Btn.add(bellBtn);

		usr_Panel.add(usr_Panel_Btn, BorderLayout.SOUTH);
		p.add(usr_Panel);

// [ [ [ [ text_Panel ] ] ] ]
		JPanel text_Panel = new JPanel();
		text_Panel.setLayout(new BorderLayout());
		text_Panel.setLocation(530, 300);
		text_Panel.setSize(200, 240);
		text_Panel.setBorder(eborder); // 경계선

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
		if (e.getSource() == tf_Talk) {				// tf_Talk에 글을 입력했을 경우
			String msg = tf_Talk.getText();
			if (msg.length() == 0) {				// 그냥 엔터 친 경우
				return;
			} 
			if (msg.length() >= 30) {				// 글이 있는 경우
				msg = msg.substring(0, 30);
			}
			writer.println("[MSG]" + msg);
			tf_Talk.setText(""); 					// 보낸 후 초기화
		} else if (e.getSource() == connectBtn) {	// 접속 버튼 액션
			name = tf_Id.getText().trim();
			if (name.length() <= 2 || name.length() > 10) {
				la_State.setText("아이디는 3~9자 사용 가능합니다.");
				tf_Id.requestFocus();
				return;
			}
			connect();
			tf_Id.setText(name);
			tf_Id.setEditable(false);
			tf_Ip.setEditable(false);
			la_State.setText("접속성공!");
			setTitle(name + "님의 할리갈리 게임");
			writer.println("[CONNECT]" + name);
			connectBtn.setEnabled(false);
			readyBtn.setEnabled(true);
			openBtn.setEnabled(false);
		} else if (e.getSource() == readyBtn) {			// 준비 버튼 액션
			if (!ready) {
				ready = true;
				writer.println("[READY]");
				readyBtn.setText("준비해제");
				la_State.setText("준비완료!");
			} else {
				ready = false;
				writer.println("[NOT READY]");
				readyBtn.setText("준비");
				la_State.setText("준비해제..");
			}
		} else if (e.getSource() == openBtn) {			// 카드뒤집기 버튼 액션
			int pl = 0;
			la_State.setText("당신이 카드를 뒤집습니다.");
			writer.println("[TURN]" + pl + "|" + name);
			pl++;
			openBtn.setEnabled(false);

		} else if (e.getSource() == bellBtn) {
			bell_Check();
			writer.println("[BELL]" + name);
		}
	}

// 인원 정보 세팅
	void playersInfo() {
		int count = model.getSize();
		la_PlayerInfo.setText("현재 " + count + "명접속");
	}

// 서버에서 보낸 플레이어 리스트를 리스트에 저장.
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

// 서버에서 뿌린 초기 카드 정보 디폴트모델에 넣기
	void cardList(String msg) 
	{

		model2.removeAllElements();
		StringTokenizer st = new StringTokenizer(msg, "]|["); 	// 구분인자로 ]|[ 로 사용해서 [] 이거 없에고 숫자랑 쉼표(,) 만 저장
		while (st.hasMoreElements()) 	// 구분인자 있을때 까지 저장
		{
			model2.addElement(st.nextToken());
		}
		System.out.println("서버에서 넘어온 카드 정보 :" + msg);
		cardsetting(model2);
	}
	
// playCard 벡터에 서버에서 온 카드정보 넣기
	void cardsetting(DefaultListModel model2) { 
		String playcard2_0, playcard2_1, playcard2_2, playcard2_3;
		String a[], b[], c[], d[];

		playcard2_0 = (String) model2.get(0);	// 문자열로 저장
		playcard2_1 = (String) model2.get(1);
		playcard2_2 = (String) model2.get(2);
		playcard2_3 = (String) model2.get(3);

		a = playcard2_0.split(", "); 			// 쉼표와 띄워쓰기 를 인자로 a 배열에 저장 -> a[14] 가됨
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
			playCard[0].add(Integer.parseInt(a[j])); // 배열의 값을 벡터에 넣음
		}

		for (int j = 0; j < a.length; j++) {
			playCard[1].add(Integer.parseInt(b[j])); // 배열의 값을 벡터에 넣음
		}

		for (int j = 0; j < a.length; j++) {
			playCard[2].add(Integer.parseInt(c[j])); // 배열의 값을 벡터에 넣음
		}

		for (int j = 0; j < a.length; j++) {
			playCard[3].add(Integer.parseInt(d[j])); // 배열의 값을 벡터에 넣음
		}
		System.out.println(" ");

		for (int i = 0; i < 4; i++) { 	// 확인차 출력
			for (int j = 0; j < 14; j++) {
				System.out.print(playCard[i].get(j) + " ");
			}
			System.out.println();
		}
		CntCardSet(); 	// 장수 Label 세팅
	}

	@Override
	public void run() {
		String msg;
		try {
			while ((msg = reader.readLine()) != null) {
		// 플레이어리스트를 받는다.
				if (msg.startsWith("[PLAYERS]")) 
				{
					nameList(msg.substring(9));
				}
		// 상대방 입장할 경우
				else if (msg.startsWith("[ENTER]")) 
				{
					model.addElement(msg.substring(7));
					playersInfo();
					ta_Info.append("[" + msg.substring(7) + "]님이 입장하였습니다.\n");
					sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
					validate();
				}
		// 접속이 끊어진경우
				else if (msg.startsWith("[DISCONNECT]")) 
				{
					model.removeElement(msg.substring(6));
					playersInfo();
					ta_Info.append("[" + msg.substring(12) + "]님이 나갔습니다.\n");
					sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
					validate();
				}
		// 게임이 시작된 경우
				else if (msg.startsWith("게임을 시작합니다.")) 
				{
					openBtn.setEnabled(false);
					bellBtn.setEnabled(true);
					readyBtn.setEnabled(false);
				}
		// 카드분배
				else if (msg.startsWith("[카드분배]")) {
					cardList(msg.substring(7));
				}
		// openBtn 활성화
				else if (msg.startsWith("[나의턴]")) {
					openBtn.setEnabled(true);
				}
		// 서버에서 카드뒤집은 뒤 다시 그리기 요청
				else if (msg.startsWith("[REPAINT]")) 
				{
					int a = msg.indexOf("|");
					int b = Integer.parseInt(msg.substring(a + 1));
					FlipCard(b);
				}
		// 카드가 없어서 죽었을 경우 받는 메세지
				else if (msg.startsWith("[DEAD]")) 
				{
					la_State.setText("당신은 죽었습니다.");
					la_State.setForeground(Color.RED);
					openBtn.setEnabled(false);
					bellBtn.setEnabled(false);
				}
		// 게임이 끝났을 경우
				else if (msg.startsWith("[END]")) {
					openBtn.setEnabled(false);
					bellBtn.setEnabled(false);
				}
		// 벨 울리기
				
				else if (msg.startsWith("[BELL]")) {
					int pl = Integer.parseInt(msg.substring(6, 7));
					String name = msg.substring(7);
					BellPressCheck(pl, name);
				}
		// 벨 잠시 비활성화, 정보 표시
				else if (msg.startsWith("[STOPBELL]")) {
					bellBtn.setEnabled(false);
					if (bell_Check) {
						try {
							Thread.sleep(1000);
							la_State.setText("성공!");
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.getMessage();
						}
					} else {
						try {
							Thread.sleep(1000);
							la_State.setText("실패!");
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.getMessage();
						}
					}
				}
		// 다시 벨 활성화
				else if (msg.startsWith("[GOBELL]")) {
					bellBtn.setEnabled(true);
				}
		// la_State에 각 메시지 출력
				else if (msg.startsWith("[SETLA]")) {
					la_State.setText(msg.substring(7));
				}
		// 
				else if (msg.startsWith("[DEADLOCK]")) {
					int a = Integer.parseInt(msg.substring(10));
					cnt_Card[a].setText("0장");;
				}
		// 채팅창에 메시지를 입력한 경우
				else // 그냥 메세지만 왔을경우 그냥 출력
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
		ta_Info.append("접속이 끊겼습니다.\n");
		sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
		validate();
	}

// 서버 연결
	void connect() {
		try {
			String ip = tf_Ip.getText();
			ta_Info.append("서버에 연결을 요청합니다.\n");
			socket = new Socket(ip, 7777);
			ta_Info.append("--연결 성공--\n");
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			new Thread(this).start();
		} catch (IOException e) {
			ta_Info.append(e + "\n\n연결 실패..\n");
		}
	}
	
// 카드 뒤집기 시에 기능들 . 숫자검사해서 맞는 카드 띄워주고, 플레이카드배열 수정, 깔린카드 임시저장
	void FlipCard(int pl) { 
		int sc;
		int temp;
		
		DieCheck(pl, name); // 수정수정
		
		if(playCard[pl].size() > 0) { //수정수정
		if (playCard[pl].firstElement() < 14) { // 바나나냐?
			if (playCard[pl].firstElement() < 5)// 1개냐?
				player_Card[pl].setIcon(banana[0]);
			else if (playCard[pl].firstElement() > 4 && playCard[pl].firstElement() < 8)// 2개냐?
				player_Card[pl].setIcon(banana[1]);
			else if (playCard[pl].firstElement() > 7 && playCard[pl].firstElement() < 11)// 3개냐?
				player_Card[pl].setIcon(banana[2]);
			else if (playCard[pl].firstElement() > 10 && playCard[pl].firstElement() < 13)// 4개냐?
				player_Card[pl].setIcon(banana[3]);
			else
				player_Card[pl].setIcon(banana[4]);
		}
		else if (playCard[pl].firstElement() >= 14 && playCard[pl].firstElement() < 28) { // 레몬이냐?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1개냐?
				player_Card[pl].setIcon(Lemon[0]);
			else if (sc > 4 && sc < 8)// 2개냐?
				player_Card[pl].setIcon(Lemon[1]);
			else if (sc > 7 && sc < 11)// 3개냐?
				player_Card[pl].setIcon(Lemon[2]);
			else if (sc > 10 && sc < 13)// 4개냐?
				player_Card[pl].setIcon(Lemon[3]);
			else
				player_Card[pl].setIcon(Lemon[4]);
		}
		else if (playCard[pl].firstElement() >= 28 && playCard[pl].firstElement() < 42) { // 복숭아냐?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1개냐?
				player_Card[pl].setIcon(Peach[0]);
			else if (sc > 4 && sc < 8)// 2개냐?
				player_Card[pl].setIcon(Peach[1]);
			else if (sc > 7 && sc < 11)// 3개냐?
				player_Card[pl].setIcon(Peach[2]);
			else if (sc > 10 && sc < 13)// 4개냐?
				player_Card[pl].setIcon(Peach[3]);
			else
				player_Card[pl].setIcon(Peach[4]);
		}
		else if (playCard[pl].firstElement() >= 42 && playCard[pl].firstElement() < 56) { // 딸기냐?
			sc = playCard[pl].firstElement() % 14;
			if (sc < 5)// 1개냐?
				player_Card[pl].setIcon(Straw[0]);
			else if (sc > 4 && sc < 8)// 2개냐?
				player_Card[pl].setIcon(Straw[1]);
			else if (sc > 7 && sc < 11)// 3개냐?
				player_Card[pl].setIcon(Straw[2]);
			else if (sc > 10 && sc < 13)// 4개냐?
				player_Card[pl].setIcon(Straw[3]);
			else
				player_Card[pl].setIcon(Straw[4]);
		}

		temp = playCard[pl].firstElement(); // 한장씩 까서 테이블(tableCard벡터)에 까는 기능
		
		if (openCard.size() == 4) { 		// 오픈 된 카드가 4개일 때 검사 해야될 카드 셋팅
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
		} //수정수정
		CntCardSet();

	}

// 벨 눌렀을 때 검사해서 득실처리
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
		if (openCardSize > 3) { // 테이블 카드 4장 이상일 때
			for (int i = openCard.size() - 1; i > openCard.size() - playerNum - 1; i--) { // 카드가 뭔과일 몇개인지 검사해서 각 변수에 계산.
				if (openCard.get(i) < 14) { // 바나나냐?
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
				else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // 레몬이냐?
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
				else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // 복숭아냐?
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
				else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // 레몬이냐?
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
		else {// 테이블 카드가 4개 미만일 때.
			for (int i = openCard.size() - 1; i >= 0; i--) { // 카드가 뭔과일 몇개인지 검사해서 각 변수에 계산.
				if (openCard.get(i) < 14) { // 바나나냐?
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
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // 레몬이냐?
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
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // 복숭아냐?
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
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // 레몬이냐?
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
		} // 각 과일변수들에 계산 완료.
	//벨 성공
		if (banana == 5 || lemon == 5 || peach == 5 || straw == 5) {
			bell_Check = true;
			for (int i = 0; i < tableCard.size(); i++)
				playCard[pl].add(tableCard.get(i));
			ta_Info.append(name + "님이 벨 울리기에 성공 .\n" + "뒤집어진 카드를 가져갑니다.\n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			tableCard.removeAllElements();		// 잘쳤으면 테이블 카드 다 주고 테이블 초기화
			openCard.removeAllElements(); 		// 뒤집어진 카드들 초기화
			CardBackSet();

		} 
	// 벨 실패
		else { 
			ta_Info.append(name + "님이 종치기에 실패했습니다.\n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			if (playCard[pl].size() == 0) {
				DieCheck(pl, name);
			} 
			else if (playCard[pl].size() < 3) { // 예외처리 3장 미만일 때. 테이블에 남은카드 깔기
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

		System.out.println("---------------------------"); // 확인차 출력구문
		for (int i = 0; i < 4; i++) {
			System.out.print("플레이어" + i + " ");
			for (int j = 0; j < playCard[i].size(); j++) {
				System.out.print(+playCard[i].get(j) + " ");
			}
			System.out.println();
		}
		System.out.print("판에 깔린 카드 ");
		for (int i = 0; i < tableCard.size(); i++) {
			System.out.print(tableCard.get(i) + " ");
		}
		System.out.println();
		System.out.print("뒤집어진 카드 ");
		System.out.println(" 바나나" + banana + " 레몬" + lemon + " 복숭아" + peach + " 딸기" + straw);
		System.out.println("---------------------------");

		CntCardSet();
	}
	
// 사망여부체크. 죽었으면 1 살았으면 0 리턴.
	int DieCheck(int pl, String name) { 
		int dieFlag = 0;
		int firstFlag = 0;

		if (playCard[pl].size() == 0) { // 가진 카드수가 0장이냐?
			player_Card[pl].setIcon(dieIcon_img);
			dieFlag = 1;
			System.out.println("플레이어" + pl + " 사망");
			ta_Info.append("한명의 플레이어가 사망하였습니다. \n");
			sc_Pane.getVerticalScrollBar().setValue(sc_Pane.getVerticalScrollBar().getMaximum());
			validate();
			writer.println("[DEAD]" + pl);
			cnt_Card[pl].setText("0장");
		}

		for (int i = 0; i < 4; i++) {
			if (playCard[i].size() > 0)
				firstFlag = firstFlag + 1;
		}

		if (firstFlag == 1) { // 1명만 생존
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
	
// 카드 뒷면 셋팅해주는 함수
	void CardBackSet() { 
		for (int i = 0; i < 4; i++) {
			player_Card[i].setIcon(cardBack_img);
			if (playCard[i].size() == 0)
				player_Card[i].setIcon(dieIcon_img);
		}
	}
	
// 장수 레이블 세팅
	void CntCardSet() { 
		String[] playcard = new String[4];
		String a[] = null, b[] = null, c[] = null, d[] = null;

		for (int i = 0; i < 4; i++) {
			playcard[i] = playCard[i].toString(); // 문자열로 저장
		}
		a = playcard[0].split(", "); // 쉼표와 띄워쓰기 를 인자로 a 배열에 저장 -> a[14] 가됨
		b = playcard[1].split(", ");
		c = playcard[2].split(", ");
		d = playcard[3].split(", ");

		cnt_Card[0].setText(a.length + "장");
		cnt_Card[1].setText(b.length + "장");
		cnt_Card[2].setText(c.length + "장");
		cnt_Card[3].setText(d.length + "장");
	}
	
// 벨 울리기 성공 실패 체크하는 메소드
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
		if (openCardSize > 3) { // 테이블 카드 4장 이상일 때
			for (int i = openCard.size() - 1; i > openCard.size() - playerNum - 1; i--) { // 카드가 뭔과일 몇개인지 검사해서 각 변수에 계산.
				if (openCard.get(i) < 14) { // 바나나냐?
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
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // 레몬이냐?
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
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // 복숭아냐?
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
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // 레몬이냐?
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
		} else {// 테이블 카드가 4개 미만일 때.
			for (int i = openCard.size() - 1; i >= 0; i--) { // 카드가 뭔과일 몇개인지 검사해서 각 변수에 계산.
				if (openCard.get(i) < 14) { // 바나나냐?
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
				} else if (openCard.get(i) >= 14 && openCard.get(i) < 28) { // 레몬이냐?
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
				} else if (openCard.get(i) >= 28 && openCard.get(i) < 42) { // 복숭아냐?
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
				} else if (openCard.get(i) >= 42 && openCard.get(i) < 56) { // 레몬이냐?
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
		} // 각 과일변수들에 계산 완료.
		if (banana == 5 || lemon == 5 || peach == 5 || straw == 5) // 벨 잘쳤냐?
			bell_Check = true;
		else
			bell_Check = false;
	}

	public static void main(String[] args) {
		new Halli_Client();
	}
}