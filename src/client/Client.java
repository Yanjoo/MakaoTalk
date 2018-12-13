package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class Client {
	
	// 로그인 한 클라이언트 정보
	private String id;
	private String password;
	private Socket socket;
	private Image profileImage;
	public static Vector<String> rooms = new Vector<String>();
	
	// 리스트로 변경 예정
	public List<HBox> friends;
	
	// 싱글톤 객체
	private static Client client;
	
	// UI 변경을 위해 실행 중인 friendControl의 객체를 얻는다
	private FriendsController friendControl;
	
	// 싱글톤 생성자
	private Client() {}
	
	private String from;
	private String to;
	private String contents;
	
	// 싱글톤 get
	public static Client getClient() {
		if (client == null) client = new Client();
		return client;
	}
	
	// 로그인한 클라이언트 데이터 설정
	public void setData(String id, String password, Socket socket) {
		this.id = id;
		this.password = password;
		this.socket = socket;

		// 이미지 설정
		FileInputStream fi = null;
		try {
			fi = new FileInputStream("C:/Temp/profile_image/" + id + ".gif");
		} catch (FileNotFoundException e) {
			try {
				fi = new FileInputStream("C:/Temp/profile_image/basic.gif");
			} catch (FileNotFoundException e1) {}
		}
		this.profileImage = new Image(fi);
		try {
			fi.close();
		} catch (IOException e) {}
	}
	
	public String getId() {
		return id;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setFriendsControl(FriendsController friendControl) {
		this.friendControl = friendControl;
	}
	
	public FriendsController getFriendsControl() {
		return friendControl;
	}
	
	public void setProfileImage(String url) {
		try {
			FileInputStream fi = new FileInputStream(url);
			this.profileImage = new Image(fi);
		} catch (FileNotFoundException e1) {}
	}
	
	public Image getProfileImage() {
		return profileImage;
	}
	
	public void setrooms() {
		try {
			File chatList = new File("C:/Temp/" + id + "_chatList.dat");
			System.out.println("C:/Temp/" + id + "_chatList.dat");
			FileReader fr = new FileReader(chatList);
			int readCharNo;
			char[] cbuf = new char[10];
			char[] read = new char[1];
			
			int index = 0;
			while ((readCharNo = fr.read(read)) != -1) {
				if (read[0] == '\n') {
					String roomName = new String(cbuf, 0, index - 1);
					index = 0;
					System.out.println("로딩 할 방 이름: " + roomName);
					rooms.add(roomName);
				} else {
					cbuf[index++] = read[0];
				}
			}
			fr.close();
		} catch (Exception e) {
			System.err.println("C:/Temp/" + id + "_chatList.dat 파일이 없음");
		}
	}
	
	// 친구추가 AddFriendsController에서 사용
	public void addFriends(String id) {
		String basicProfile = "C:/Temp/profile_image/basic.gif";
		String myProfile = "C:/Temp/profile_image/" + id.trim() + ".gif";
		
		FileInputStream fis = null;
		try {
			fis	= new FileInputStream(myProfile);
		} catch(Exception e) {
			try {
				fis = new FileInputStream(basicProfile);
			} catch (FileNotFoundException e1) {}
		}
		 
		Image image = new Image(fis);
		
		ImageView profileImage = new ImageView(image);
        profileImage.setFitHeight(32);
        profileImage.setFitWidth(32);
        
        Label label = new Label(id);

		HBox x = new HBox();
		x.setSpacing(10);
		x.getChildren().addAll(profileImage, label);
		x.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				friendControl.setClickedname(label.getText());
					if (event.getClickCount() > 1) {
						friendControl.chatStart(label.getText());
					}
			}
		});
		friends.add(x);
	}
	
	// 로그인 성공시 파일 입출력으로 friends 리스트 추가
	public void setFriends() {
		File friendsData = new File("C:/Temp/" + id + "_Friends.dat");
		friends = new ArrayList<HBox>();

		try {
			FileReader fr = new FileReader(friendsData);
			int readCharNo;
			char[] cbuf = new char[10];
			char[] read = new char[1];
			
			int index = 0;
			
			while ((readCharNo=fr.read(read)) != -1) {
				if (read[0] == '\n') {
					String friendName = new String(cbuf, 0, index).trim();
					index = 0;
					
					System.out.println(friendName);
					
					// 친구 이름으로 된 gif 파일이 있으면 설정하고 없으면 기본으로 한다
					String basicProfile = "C:/Temp/profile_image/basic.gif";
					String friendProfile = "C:/Temp/profile_image/" + friendName.trim() + ".gif";
					
					// client/images에 친구 이름으로 된 프로필 사진 만들기				
					FileInputStream fi = null;
					try {
						fi = new FileInputStream(friendProfile);
					} catch (FileNotFoundException e) {
						try {
							fi = new FileInputStream(basicProfile);
						} catch (FileNotFoundException e1) {}
					}
					
					
					ImageView profileImage = new ImageView(new Image(fi));
					profileImage.setFitHeight(32);
			        profileImage.setFitWidth(32);
			        
			        Label label = new Label(friendName);

					HBox x = new HBox();
					x.setSpacing(10);
					x.getChildren().addAll(profileImage, label);
					x.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							friendControl.setClickedname(label.getText());
								if (event.getClickCount() > 1) {
									friendControl.chatStart(label.getText());
								}
						}
					});
					friends.add(x);

					System.out.println("친구 설정: " + friendName);
				}  else {
					cbuf[index++] = read[0];
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 	
	// 로그인에 성공하면 메시지 수신 시작
	public void start() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						System.out.println(id + " 데이터 수신 대기중... " + socket.toString());
						byte[] byteArr = new byte[100];
						InputStream is = socket.getInputStream();
						int readByteCount = is.read(byteArr);
						
						if (readByteCount == -1) {
							throw new Exception();
						}
						
						String data = new String(byteArr, 0, readByteCount, "utf-8");
						System.out.println("받은 데이터: " + data);
						
						String[] datas = data.split("/");
						from = datas[2];
						to = datas[0];
						contents = datas[1];
						
						// 데이터 수신을 알린다, 팝업 알림
						friendControl.receive(from, contents);
						// 받은 메시지를 대화 창에 보여준다
						friendControl.setMessage(from, contents);
						
					} catch(NullPointerException e) {
						// 상대가 보낸 대화들을 저장한다
						System.err.println("클라이언트 대화 방이 안 열려 있음 ");
						System.out.println(to + " " + from);
						try {
							File chatData = new File("C:/Temp/" + to.trim() + "To" + from.trim() + ".txt");
							FileWriter fw = new FileWriter(chatData, true);
							
							fw.write(from + "/" + contents +"\r\n");
							fw.flush();
							fw.close();
							
						} catch (Exception e2) {
							e.printStackTrace();
						}
					} catch(SocketException e) {
						System.err.println("클라이언트 로그아웃 ");
						break;
					} catch (Exception e) {
						System.err.println("클라이언트 메시지 수신 실패");
						e.printStackTrace();
						break;
					}
				}
			}
		};
		thread.start();
	}
	
	// 클라이언트 종료
	public void stop() {
		try {
			// 서버에 로그아웃 사실을 알린다
			OutputStream os = socket.getOutputStream();
			byte[] data = "logout".getBytes("utf-8");
			os.write(data);
			os.flush();
		} catch (IOException e) {}
		
		// 소켓 제거
		if(!socket.isClosed() && socket != null) {
			try {
				socket.close();
			} catch (IOException e) {}
			System.out.println(id + " 로그아웃 성공");
		}
	}
}
