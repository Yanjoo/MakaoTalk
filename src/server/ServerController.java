package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ServerController implements Initializable  {

	private @FXML Button button; // 서버 시작, 중지 버튼
	private @FXML TextArea textArea; // 서버의 실행 로그
	
	// 스레드 작업을 위한 스레드 풀
	private ExecutorService executorService;
	// 서버 소켓
	private ServerSocket serverSocket;
	// 가입한 회원들을 저장하는 loginDatas, 검색 할 경우 사용한다.
	private TreeMap<String, String> loginDatas = new TreeMap<>();
	// 현재 로그인한 회원들을 저장할 자료구조
	private List<Client> connections = new Vector<>();
	// DB 연결 객체
	private Connection conn;
	
	public void displayText(String message) {
		textArea.appendText(message + "\n");
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 버튼을 누르면 서버 시작, 다시 누르면 서버 종료
		button.setOnAction(e -> {
			if (button.getText().equals("서버 시작")) {
				serverStart();
			} else {
				serverStop();
			}
		});
		
		// 서버에선 텍스트를 입력하지 못하게 한다
		textArea.setEditable(false);
	}
	
	// 서버 시작
	// 서버 시작 버튼 클릭 이벤트
	public void serverStart() {
		// 스레드풀 생성, 스레드 풀은 서버의 과부하를 막아주는 좋은 친구
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
		try {
			// 서버 소켓 생성
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", 5001));
			System.out.println("서버 ip 주소: " + serverSocket.getLocalSocketAddress());
		} catch (Exception e) {
			if (!serverSocket.isClosed()) serverStop();
			return;
		}
		
		try {
			// logindataDB 연결
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/makaotalkdb", "makaotalk", "");
			String sql = "select id, password from logindata";
			PreparedStatement pstmt = conn.prepareStatement(sql);

			ResultSet rs = pstmt.executeQuery();
			// db에 저장되어 있는 정보를 loginDatas 트리로 복사한다
			while (rs.next()) {
				String id = rs.getString("id");
				String password = rs.getString("password");
				System.out.println("가입 데이터: " + id + "/" + password);
				loginDatas.put(id, password);
			}
			
			rs.close();
			pstmt.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// 서버의 작업
		// runnable로 작업을 만든다, 서버는 계속 작동하면서 로그인과 회원가입, 친구 추가 기능을 처리 해준다
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					// UI 변경, 작업 스레드에서 ui를 변경하기 위해선 Platform.runLater를 써야함
					Platform.runLater(() -> {
						button.setText("서버 멈춤");
					});
					
					Socket socket = null;
					try {
						// 소켓 생성 (로그인, 회원가입 작업)
						socket = serverSocket.accept();
						// 회원가입, 로그인, 친구 추가 요청처리
						byte[] getIdPassword = new byte[100];
						InputStream is = socket.getInputStream();
						int readByteCount = is.read(getIdPassword);
						
						// 데이터 수신 실패시 예외 처리
						if (readByteCount == -1) throw new IOException();
						
						String data = new String(getIdPassword, 0, readByteCount, "UTF-8");
						
						// J L A 구분(join, login, add)
						char isLoginOrJoin = data.charAt(0);
						// 받은 데이터 분석
						String[] datas = data.split("/");
						String id = datas[1];
						System.out.println("받은 요청: " + data);
						
						// switch 문에서 사용하는 변수들
						byte[] resultByte;
						OutputStream os;
						String message, result;
						
						switch (isLoginOrJoin) {
						case 'J':
							message = id + " [회원가입 요청] " + socket.getRemoteSocketAddress();
							Platform.runLater(() -> displayText(message));
							// 회원가입 처리
							join(data);
							break;
							
						case 'L':
							message = id + " [로그인 요청] " + socket.getRemoteSocketAddress();
							Platform.runLater(() -> displayText(message));
							// 로그인 처리, success or fail 반환
							result = login(data);
							// 로그인 처리 결과를 클라이언트에게 다시 보내준다
							resultByte = result.getBytes("utf-8");
							os = socket.getOutputStream();
							os.write(resultByte);
							os.flush();
							
							// 로그인 성공하면 connections에 추가
							if (result.equals("success")) {
								Client client = new Client(socket, id);
								connections.add(client);
							}
							break;
							
						case 'A':
							String myName = datas[2];
							message = myName + " [친구 추가 요청] " + socket.getRemoteSocketAddress();
							Platform.runLater(() -> displayText(message));
							// 친구 추가 요청
							result = addFriend(data);
							resultByte = result.getBytes("utf-8");
							// 친구 추가 결과를 알려줌
							os = socket.getOutputStream();
							os.write(resultByte);
							os.flush();
							break;
							
						case 'C':
							String toChatName = datas[2];
							File chatList = new File("C:/Temp/" + id + "_chatList.dat");
							FileWriter fw = new FileWriter(chatList, true);
							fw.write(toChatName + "\r\n");
							fw.flush();
							fw.close();
							break;
						}
					} catch (Exception e) {
						if (!serverSocket.isClosed()) serverStop();
						break;
					}
				}
			}
		};
		executorService.submit(runnable);
	
		Platform.runLater(() ->  displayText("[서버 실행]")); 
	}
	
	// 서버 종료
		public void serverStop() {
			try {
				Iterator<Client> iterator = connections.iterator();
				while (iterator.hasNext()) {
					Client client = iterator.next();
					client.socket.close();
					iterator.remove();
				}
				if (serverSocket != null && !serverSocket.isClosed()) 
					serverSocket.close();
				if (executorService != null && !executorService.isShutdown()) 
					executorService.shutdown();
				Platform.runLater(() -> {
					displayText("서버 멈춤");
					button.setText("서버 시작");
				});
				// 디비 연결 종료
				conn.close();
				System.err.println("DB 연결 종료");
			} catch(Exception e) {}
		}
	
	// 회원 가입, 파일에 id와 pw 저장 -> DB로 수정
	private void join(String data) {
		// data를 분리해서 id 와 pw를 따로 분리
		String[] datas = data.split("/");
		String id = datas[1];
		String password = datas[2];
		
		loginDatas.put(id, password);
		
		try {
			// logindataDB에 가입한 정보 저장
			PreparedStatement pstmt = null;
			int row = 0;

			String sql = "insert into logindata(id, password) values(?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, password);

			row = pstmt.executeUpdate();
			pstmt.close();
			System.out.println(id + " " + password + " db에 추가 완료");
			
			// 기본 친구
			File friendsData = new File("C:/Temp/" + id + "_Friends.dat");
			FileWriter fw2 = new FileWriter(friendsData, true);
			fw2.write("YANJU\r\n");
			fw2.flush();
			fw2.close();
			
			
		} catch(Exception e) {}
	}
	
	// loginDats 트리에서 데이터를 찾음
	private String login(String data) {
		// data를 분리해서 id 와 pw를 따로 분리
		String[] datas = data.split("/");
		String id = datas[1];
		String password = datas[2].trim();
		
		System.out.println("로그인 요청 ID: " + id + " 비밀번호: " + password.length());
		
		String result = "fail";
		boolean idTrue = loginDatas.containsKey(id);
		if (idTrue) {
			String comPw = loginDatas.get(id);
			boolean pwTrue = comPw.equals(password);
			//System.out.println(comPw.length());
			if (pwTrue) {
				result = "success";
			}
		}
		System.out.println("로그인 결과: " + idTrue);
		
		return result;
	}
	
	// 친구 추가
	private String addFriend(String data) {
		// 요청한 친구 id가 서버에 저장되 있으면 success 반환
		String[] datas = data.split("/");
		String friend = datas[1];
		String me = datas[2];
		
		System.out.println(friend);
		
		String result = "fail";
		
		boolean idTrue = loginDatas.containsKey(friend);
		if (idTrue) result = "success";
		
		if (result.equals("fail")) {
			File friendsData = new File("C:/Temp/" + me + "_Friends.dat");
			try {
				FileWriter fw = new FileWriter(friendsData, true);
				fw.write("");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		} else {
			File friendsData = new File("C:/Temp/" + me + "_Friends.dat");
			try {
				FileWriter fw = new FileWriter(friendsData, true);
				fw.write(friend + "\r\n");
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.err.println("친구 추가: " + result);
			return result;
		}
	}
	
	// 서버의 클라이언트 클래스, 현재 로그인 해 있는 클라이언트들의 정보를 서버도 알고 있어야한다
	private class Client {
		private Socket socket;
		private String id;
		private Image image;
		
		public Client(Socket socket, String id) {
			this.socket = socket;
			this.id = id;
			
			System.out.println(id + " 로그인 " + socket);
			// 로그인 즉시 수신을 시작한다.
			receive();
		}
		
		private String getId() {
			return id;
		}
		
		private Socket getSocket() {
			return socket;
		}
		
		// 받은 데이터를 수신자에게 전달한다
		private void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						while(true) {
							byte[] getData = new byte[100];
							InputStream is = socket.getInputStream();
							int readByteCount = is.read(getData);
							
							String data = new String(getData, 0, readByteCount, "utf-8");
							System.out.println("받은 데이터: " + data);
							
							// 클라이언트의 로그아웃 요청
							if (data.equals("logout")) {
								Platform.runLater(() -> displayText(id + "로그아웃 요청 " + socket.getRemoteSocketAddress()));
								System.out.println(id + " 로그아웃");
								// 로그인한 클라이언트를 저장하는 list에서 제거
								connections.remove(Client.this);
								// 연결된 socket을 끊음
								socket.close();
								break;
							}
							
							String message = "[메시지 전송 요청] " + socket.getRemoteSocketAddress();
							Platform.runLater(() -> displayText(message));
							
							String[] datas = data.split("/");
							String to = datas[0];
							
							// 서버에 접속되 있는 클라이언트중에서 수신자가 있는지 확인한다.
							Iterator<Client> iter = connections.iterator();
							while (iter.hasNext()) {
								Client client = iter.next();
								// 찾았으면 데이터를 전송한다
								if (client.getId().equals(to)) {
									Socket receiver = client.getSocket();
									System.out.println("받는 사람: " + client.getId() + " " + receiver.toString());
									OutputStream os = receiver.getOutputStream();
									byte[] messageBox = data.getBytes("UTF-8");
									os.write(messageBox);
									os.flush();
									System.out.println("전송 완료");
									break;
								}
							}
						}
					} catch(Exception e) {
						System.err.println("서버 전송 실패");
						//e.printStackTrace();
					}
				}
				
			};
			
			executorService.submit(runnable);
		}
	}
}
