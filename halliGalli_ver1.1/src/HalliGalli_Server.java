import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;

import javax.swing.JLabel;

public class HalliGalli_Server
{
	/// Field
	ServerSocket		server;
	Random				rnd = new Random(); 
	int					Card[]				= new int[56];		// 카드 섞기위한 변수
	int					TurnCard[][]		= new int[4][];		// 뒤집어진 카드를 저장하는 변수
	int					TurnCardCount[]		= new int[4];		// 뒤집어진 카드의 개수
	int					CardType[]			= new int[4];		// 카드의 종류를 알기위한 변수
	int					CardNum[]			= new int[4];		// 카드속 과일 개수 알기위한 변수
	int					ClientCard[][]		= new int[4][];		// 클라이언트 카드 변수
	int					ClientCardCount[]	= new int[4];		// 클라이언트 카드 개수 변수
	int					NowPlayer;								// 현재 차례가 누구인지 저장
	boolean				isSuccess = false;						// 종치기에 성공했는지 확인
	boolean				dead[] = new boolean[4];				// 죽었는지 살았는지 확인
	boolean				EndGame = false;						// 게임이 끝인지 확인.
	boolean				isBell = false;							// 상대방이 종을 쳤는지 확인
	String				Player[] = new String[4];				// 플레이어이름 접속순서대로 저장
	BManager			bMan = new BManager();					// 클라이언트에게 방송해주는 객체
	int					n = 0;
	int[] 				cardNum; //전체카드번호
	JLabel[] 			player_Card;
	Vector<Integer>[]	playCard;
	
	/// Constructor
	public HalliGalli_Server()
	{
	}

	/// Method
	public void startServer()
	{
		try
		{
			server = new ServerSocket(7777, 4);
			System.out.println("서버소켓이 생성되었습니다.");
			while (true)
			{
				// 클라이언트와 연결된 스레드를 얻는다.
				Socket socket = server.accept();

				// 스레드를 만들고 실행시킨다.
				HalliGalli_Thread ht = new HalliGalli_Thread(socket);
				ht.start();

				// bMan에 스레드를 추가한다.
				bMan.add(ht);

				System.out.println("접속자 수: " + bMan.size());
			}
		}
		catch (Exception e)
		{}
	}

	public void setCard() { //카드 생성후 섞기 
		int temp;
		int r;
		cardNum = new int[56]; 
		playCard = new Vector[4];
		
		for(int i=0; i<4; i++) 
		{
			dead[i] = false;
			playCard[i] = new Vector();
			ClientCard[i] = new int[56];
		}
	
		
		for(int i=0; i<56; i++) //번호입력
		{ 						
			cardNum[i] = i;
		}
		
		for(int i=0; i<56; i++) //번호 섞기 
		{  
			temp = cardNum[i];
			r = (int)(Math.random()*55);
			cardNum[i] = cardNum[r];
			cardNum[r] = temp;
		}
				
	}

	public void DivideCard()	// 카드정보를  playCard 벡터에 나눠서 저장함 
	{	
		for(int i=0; i<4; i++)  //playCard[0][1][2][3] 에 각각 14개씩 분배 
		{			
			playCard[i].removeAllElements(); //분배전 초기화 
			for(int j=0; j<14; j++) 
			{
				playCard[i].add(cardNum[i*14+j]); //= cardNum[i*14+j];	
				ClientCard[i][j] = cardNum[i*14+j];
				ClientCardCount[i]++;
			}
		}	//	ClientCard[i][j] = Card[i*14+j];						
	}
	
	public void giveCard() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<4; i++) 
		{
			sb.append(playCard[i] + "|");
		}
		
		bMan.sendToAll("[카드분배]"+sb);
	}

	public void NextPlayer() //3번째 플레이어 다음 플레이어가 옳바르기위해 
	{
		NowPlayer++;
		if (NowPlayer == 4) // 0 1 2 3 만 있어야되니깐 4되면 0 으로 
		{
			NowPlayer = 0;
		}

		while (dead[NowPlayer]) //다음플레이어가 죽은사람이면 +1 로 다음차례로 넘긴다 
		{
			NowPlayer++;
			if (NowPlayer == 4)
			{
				NowPlayer = 0;
			}
		}
	}
	
	public static void main(String[] args)
	{
		HalliGalli_Server server = new HalliGalli_Server();
		server.startServer();
	}

	class HalliGalli_Thread extends Thread
	{
		/// Field
		String			userName = null;		// 이름
		Socket			socket;					// 서버소켓
		boolean			ready = false;			// 준비여부
		BufferedReader	reader;					// 받기
		PrintWriter		writer;					// 보내기

		/// Constructor
		HalliGalli_Thread(Socket socket)
		{
			this.socket = socket;
		}

		Socket getSocket()
		{
			return socket;
		}

		String getUserName()
		{
			return userName;
		}

		boolean isReady()
		{
			return ready;
		}

		public void run()
		{
			try
			{
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);

				String msg;

				while ((msg = reader.readLine()) != null)
				{
					if (msg.startsWith("[CONNECT]"))
					{
						if (bMan.isFull())
						{
							bMan.remove(this);
							if (reader != null)
							{
								reader.close();
							}
							if (writer != null)
							{
								writer.close();
							}
							if (socket != null)
							{
								socket.close();
							}
							reader = null;
							writer = null;
							socket = null;
							System.out.println(userName + "님이 접속을 끊었습니다.");
							System.out.println("접속자 수: " + bMan.size());
							bMan.sendToAll("[DISCONNECT]" + userName);
							bMan.sendToAll("[SETLA]" + "[" + userName +"]님이 접속을 끊었습니다.");
						}
						else
						{	// 제대로 접속이 된경우
							userName = msg.substring(9);
							bMan.sendToAll("[ENTER]" + userName);
							bMan.sendToAll(bMan.getNames());
							n++;
						}
					}

					else if (msg.startsWith("[READY]"))					// 클라이언트에서 레디한 경우
					{
						ready = true;
						bMan.sendToAll(userName + "님 준비완료!");
						if (bMan.isReady())
						{
							bMan.sendToAll("게임을 시작합니다.");
							setCard();										// 게임초기화
							DivideCard();									// 카드 분배 
							giveCard();										// 클라이언트에게 카드 정보 넘기기 
							for (int i=0; i<bMan.size(); i++)						// 플레이어 이름을 저장
							{
								Player[i] = bMan.getHT(i).getUserName();
							}
							NowPlayer = 0;											// 게임시작은 0번부터
							bMan.sendToAll(Player[NowPlayer] + "님 차례입니다.");
							bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "님 차례입니다.");
							bMan.sendTo(NowPlayer,"[나의턴]");
							bMan.sendTo(NowPlayer,"[SETLA]"+"[나의턴]");
						}
					}

					else if (msg.startsWith("[NOREADY]"))			// 클라이언트에서 레디를 다시 해제했을 경우
					{
						ready = false;
						bMan.sendToAll(userName + "님이 레디를 해제했습니다.");
					}
					else if (msg.startsWith("[TURN]"))				// 클라이언트에서 카드뒤집기를 했을 경우
					{
						int a = msg.indexOf("|"); 
						if (Player[NowPlayer].equals(msg.substring(a+1)))	// 자기차례여부검사
						{										
							
							bMan.sendToAll("[REPAINT]" + "|"+ NowPlayer);
							NextPlayer();
							bMan.sendToAll(Player[NowPlayer] + "님 차례입니다.");
							bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "님 차례입니다.");
							bMan.sendTo(NowPlayer, "[나의턴]");
							bMan.sendTo(NowPlayer,"[SETLA]"+"[나의턴]");
						}
						else
						{	// 자기 차례가 아닐 경우 보내는 메시지.
							writer.println("당신차례가 아닙니다.");
						}
					}
					else if(msg.startsWith("[DEAD]")) 
					{	
						int num = Integer.parseInt(msg.substring(6)); // 죽은사람의 PL 즉 죽은사람의 순서번호 
						dead[num] = true;
						bMan.sendTo(num, "[DEAD]"); // 죽은 num 번째 클라이언트에게 죽음을 알리고 버튼 비활성화  근데막얀 죽은사람이 카드 뒤집는 턴이라면? 다음사람의 버튼을 활성화 시켜야됨					
						bMan.sendToAll("[DEADLOCK]"+num);
						if(dead[num] == true && NowPlayer == num) 
						{
							NextPlayer();
							bMan.sendToAll(Player[NowPlayer] + "님 차례입니다.");
							bMan.sendTo(NowPlayer, "[나의턴]"); // 다음턴 사람 뒤집기 버튼 활성화 시키기 
						}
						else 
						{
							bMan.sendToAll(Player[NowPlayer] + "님 차례입니다.");
							bMan.sendTo(NowPlayer, "[나의턴]"); // 다음턴 사람 뒤집기 버튼 활성화 시키기 
						}
						
					}
					else if(msg.startsWith("[WIN]")) // 우승자 나왔을때 
					{
						int num = Integer.parseInt(msg.substring(5)); // 우승자 번호 저장 
						bMan.sendToAll(Player[num]+"님이 승리하셨습니다.");
						bMan.sendToAll("[SETLA]"+Player[num]+"님이 승리하셨습니다.");
						bMan.sendToAll("[END]");
					}
					else if(msg.startsWith("[MSG]"))				// 클라이언트에서 메세지를 받았을 때
					{
						bMan.sendToAll("[" + userName + "]:" + msg.substring(5));
					}										
					else if (msg.startsWith("[BELL]"))				// 클라이언트에서 벨을 울렸을 때
					{	
						String name = msg.substring(6);
						int pl = 0;
						for(int i=0; i<Player.length; i++) 
						{
							if(Player[i].equals(name))
							{ 										//player[] = 플레이어 이름들이 저장된 배열 
								pl = i;
							}
						}
						bMan.sendToAll("[SETLA]" + name + "님이 벨을 울렸습니다.");
						bMan.sendTo(pl,"[SETLA]" + "당신이 벨을 울렸습니다.");
						
						bMan.sendToAll("[STOPBELL]");
						bMan.sendToAll("[GOBELL]");
						
						bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "님 차례입니다.");
						bMan.sendTo(NowPlayer,"[SETLA]"+"[나의턴]");
						bMan.sendToAll("[BELL]"+pl+name); //모든 클라이언트한테 누른
						Thread.sleep(1000);
					}
				}
			}
			catch (Exception e)
			{
			}
			finally
			{
				try
				{
					bMan.remove(this);
					if (reader != null)
					{
						reader.close();
					}
					if (writer != null)
					{
						writer.close();
					}
					if (socket != null)
					{
						socket.close();
					}
					reader = null;
					writer = null;
					socket = null;
					System.out.println(userName + "님이 접속을 끊었습니다.");
					System.out.println("접속자 수: " + bMan.size());
					bMan.sendToAll("[DISCONNECT]" + userName);
					bMan.sendToAll("[SETLA]" + "[" + userName + "] 님이 접속을 끊었습니다.");
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	// 클래스를 저장하는 Vector를 상속받아서 스레드들을 저장하는 클래스
	class BManager extends Vector
	{
		public BManager()
		{
		}

		HalliGalli_Thread getHT(int i)		// 현재 번호의 스레드를 저장.
		{
			return (HalliGalli_Thread)elementAt(i);
		}

		Socket getSocket(int i)				// 소켓을 가져온다.
		{
			return getHT(i).getSocket();
		}

		void sendTo(int i, String msg)		// i번스레드에 메시지를 전달.
		{
			try
			{
				PrintWriter pw = new PrintWriter(getSocket(i).getOutputStream(), true);
				pw.println(msg);
			}
			catch (Exception e)
			{
			}
		}

		synchronized boolean isFull()		// 서버가 다 찼는지 확인
		{
			if (size() >= 5)
			{
				return true;
			}
			return false;
		}

		void sendToAll(String msg)			// 모든 스레드에게 보내는 메시지
		{
			for (int i=0; i<size(); i++)
			{
				sendTo(i, msg);
			}
		}

		synchronized boolean isReady()	// 전부준비여부확인
		{
			int count = 0;
			for (int i=0; i<size(); i++)
			{
				if (getHT(i).isReady())
				{
					count++;
				}
			}
			if (count == 4)
			{
				return true;
			}
			return false;
		}

		String getNames()		// 현재 접속된 스레드의 이름을 가져옴.
		{
			StringBuffer sb = new StringBuffer("[PLAYERS]");
			for (int i=0; i<size(); i++)
			{
				sb.append(getHT(i).getUserName() + "\t");
			}
			return sb.toString();
		}
	}
}

