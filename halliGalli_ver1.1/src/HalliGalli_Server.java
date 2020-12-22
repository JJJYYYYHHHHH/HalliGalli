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
	int					Card[]				= new int[56];		// ī�� �������� ����
	int					TurnCard[][]		= new int[4][];		// �������� ī�带 �����ϴ� ����
	int					TurnCardCount[]		= new int[4];		// �������� ī���� ����
	int					CardType[]			= new int[4];		// ī���� ������ �˱����� ����
	int					CardNum[]			= new int[4];		// ī��� ���� ���� �˱����� ����
	int					ClientCard[][]		= new int[4][];		// Ŭ���̾�Ʈ ī�� ����
	int					ClientCardCount[]	= new int[4];		// Ŭ���̾�Ʈ ī�� ���� ����
	int					NowPlayer;								// ���� ���ʰ� �������� ����
	boolean				isSuccess = false;						// ��ġ�⿡ �����ߴ��� Ȯ��
	boolean				dead[] = new boolean[4];				// �׾����� ��Ҵ��� Ȯ��
	boolean				EndGame = false;						// ������ ������ Ȯ��.
	boolean				isBell = false;							// ������ ���� �ƴ��� Ȯ��
	String				Player[] = new String[4];				// �÷��̾��̸� ���Ӽ������ ����
	BManager			bMan = new BManager();					// Ŭ���̾�Ʈ���� ������ִ� ��ü
	int					n = 0;
	int[] 				cardNum; //��üī���ȣ
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
			System.out.println("���������� �����Ǿ����ϴ�.");
			while (true)
			{
				// Ŭ���̾�Ʈ�� ����� �����带 ��´�.
				Socket socket = server.accept();

				// �����带 ����� �����Ų��.
				HalliGalli_Thread ht = new HalliGalli_Thread(socket);
				ht.start();

				// bMan�� �����带 �߰��Ѵ�.
				bMan.add(ht);

				System.out.println("������ ��: " + bMan.size());
			}
		}
		catch (Exception e)
		{}
	}

	public void setCard() { //ī�� ������ ���� 
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
	
		
		for(int i=0; i<56; i++) //��ȣ�Է�
		{ 						
			cardNum[i] = i;
		}
		
		for(int i=0; i<56; i++) //��ȣ ���� 
		{  
			temp = cardNum[i];
			r = (int)(Math.random()*55);
			cardNum[i] = cardNum[r];
			cardNum[r] = temp;
		}
				
	}

	public void DivideCard()	// ī��������  playCard ���Ϳ� ������ ������ 
	{	
		for(int i=0; i<4; i++)  //playCard[0][1][2][3] �� ���� 14���� �й� 
		{			
			playCard[i].removeAllElements(); //�й��� �ʱ�ȭ 
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
		
		bMan.sendToAll("[ī��й�]"+sb);
	}

	public void NextPlayer() //3��° �÷��̾� ���� �÷��̾ �ǹٸ������� 
	{
		NowPlayer++;
		if (NowPlayer == 4) // 0 1 2 3 �� �־�ߵǴϱ� 4�Ǹ� 0 ���� 
		{
			NowPlayer = 0;
		}

		while (dead[NowPlayer]) //�����÷��̾ ��������̸� +1 �� �������ʷ� �ѱ�� 
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
		String			userName = null;		// �̸�
		Socket			socket;					// ��������
		boolean			ready = false;			// �غ񿩺�
		BufferedReader	reader;					// �ޱ�
		PrintWriter		writer;					// ������

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
							System.out.println(userName + "���� ������ �������ϴ�.");
							System.out.println("������ ��: " + bMan.size());
							bMan.sendToAll("[DISCONNECT]" + userName);
							bMan.sendToAll("[SETLA]" + "[" + userName +"]���� ������ �������ϴ�.");
						}
						else
						{	// ����� ������ �Ȱ��
							userName = msg.substring(9);
							bMan.sendToAll("[ENTER]" + userName);
							bMan.sendToAll(bMan.getNames());
							n++;
						}
					}

					else if (msg.startsWith("[READY]"))					// Ŭ���̾�Ʈ���� ������ ���
					{
						ready = true;
						bMan.sendToAll(userName + "�� �غ�Ϸ�!");
						if (bMan.isReady())
						{
							bMan.sendToAll("������ �����մϴ�.");
							setCard();										// �����ʱ�ȭ
							DivideCard();									// ī�� �й� 
							giveCard();										// Ŭ���̾�Ʈ���� ī�� ���� �ѱ�� 
							for (int i=0; i<bMan.size(); i++)						// �÷��̾� �̸��� ����
							{
								Player[i] = bMan.getHT(i).getUserName();
							}
							NowPlayer = 0;											// ���ӽ����� 0������
							bMan.sendToAll(Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendTo(NowPlayer,"[������]");
							bMan.sendTo(NowPlayer,"[SETLA]"+"[������]");
						}
					}

					else if (msg.startsWith("[NOREADY]"))			// Ŭ���̾�Ʈ���� ���� �ٽ� �������� ���
					{
						ready = false;
						bMan.sendToAll(userName + "���� ���� �����߽��ϴ�.");
					}
					else if (msg.startsWith("[TURN]"))				// Ŭ���̾�Ʈ���� ī������⸦ ���� ���
					{
						int a = msg.indexOf("|"); 
						if (Player[NowPlayer].equals(msg.substring(a+1)))	// �ڱ����ʿ��ΰ˻�
						{										
							
							bMan.sendToAll("[REPAINT]" + "|"+ NowPlayer);
							NextPlayer();
							bMan.sendToAll(Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendTo(NowPlayer, "[������]");
							bMan.sendTo(NowPlayer,"[SETLA]"+"[������]");
						}
						else
						{	// �ڱ� ���ʰ� �ƴ� ��� ������ �޽���.
							writer.println("������ʰ� �ƴմϴ�.");
						}
					}
					else if(msg.startsWith("[DEAD]")) 
					{	
						int num = Integer.parseInt(msg.substring(6)); // ��������� PL �� ��������� ������ȣ 
						dead[num] = true;
						bMan.sendTo(num, "[DEAD]"); // ���� num ��° Ŭ���̾�Ʈ���� ������ �˸��� ��ư ��Ȱ��ȭ  �ٵ����� ��������� ī�� ������ ���̶��? ��������� ��ư�� Ȱ��ȭ ���Ѿߵ�					
						bMan.sendToAll("[DEADLOCK]"+num);
						if(dead[num] == true && NowPlayer == num) 
						{
							NextPlayer();
							bMan.sendToAll(Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendTo(NowPlayer, "[������]"); // ������ ��� ������ ��ư Ȱ��ȭ ��Ű�� 
						}
						else 
						{
							bMan.sendToAll(Player[NowPlayer] + "�� �����Դϴ�.");
							bMan.sendTo(NowPlayer, "[������]"); // ������ ��� ������ ��ư Ȱ��ȭ ��Ű�� 
						}
						
					}
					else if(msg.startsWith("[WIN]")) // ����� �������� 
					{
						int num = Integer.parseInt(msg.substring(5)); // ����� ��ȣ ���� 
						bMan.sendToAll(Player[num]+"���� �¸��ϼ̽��ϴ�.");
						bMan.sendToAll("[SETLA]"+Player[num]+"���� �¸��ϼ̽��ϴ�.");
						bMan.sendToAll("[END]");
					}
					else if(msg.startsWith("[MSG]"))				// Ŭ���̾�Ʈ���� �޼����� �޾��� ��
					{
						bMan.sendToAll("[" + userName + "]:" + msg.substring(5));
					}										
					else if (msg.startsWith("[BELL]"))				// Ŭ���̾�Ʈ���� ���� ����� ��
					{	
						String name = msg.substring(6);
						int pl = 0;
						for(int i=0; i<Player.length; i++) 
						{
							if(Player[i].equals(name))
							{ 										//player[] = �÷��̾� �̸����� ����� �迭 
								pl = i;
							}
						}
						bMan.sendToAll("[SETLA]" + name + "���� ���� ��Ƚ��ϴ�.");
						bMan.sendTo(pl,"[SETLA]" + "����� ���� ��Ƚ��ϴ�.");
						
						bMan.sendToAll("[STOPBELL]");
						bMan.sendToAll("[GOBELL]");
						
						bMan.sendToAll("[SETLA]" + Player[NowPlayer] + "�� �����Դϴ�.");
						bMan.sendTo(NowPlayer,"[SETLA]"+"[������]");
						bMan.sendToAll("[BELL]"+pl+name); //��� Ŭ���̾�Ʈ���� ����
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
					System.out.println(userName + "���� ������ �������ϴ�.");
					System.out.println("������ ��: " + bMan.size());
					bMan.sendToAll("[DISCONNECT]" + userName);
					bMan.sendToAll("[SETLA]" + "[" + userName + "] ���� ������ �������ϴ�.");
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	// Ŭ������ �����ϴ� Vector�� ��ӹ޾Ƽ� ��������� �����ϴ� Ŭ����
	class BManager extends Vector
	{
		public BManager()
		{
		}

		HalliGalli_Thread getHT(int i)		// ���� ��ȣ�� �����带 ����.
		{
			return (HalliGalli_Thread)elementAt(i);
		}

		Socket getSocket(int i)				// ������ �����´�.
		{
			return getHT(i).getSocket();
		}

		void sendTo(int i, String msg)		// i�������忡 �޽����� ����.
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

		synchronized boolean isFull()		// ������ �� á���� Ȯ��
		{
			if (size() >= 5)
			{
				return true;
			}
			return false;
		}

		void sendToAll(String msg)			// ��� �����忡�� ������ �޽���
		{
			for (int i=0; i<size(); i++)
			{
				sendTo(i, msg);
			}
		}

		synchronized boolean isReady()	// �����غ񿩺�Ȯ��
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

		String getNames()		// ���� ���ӵ� �������� �̸��� ������.
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

