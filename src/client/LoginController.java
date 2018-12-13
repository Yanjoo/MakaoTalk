package client;

import java.io.FileNotFoundException;

/*
 *  로그인 화면 컨트롤러
 * 
 * */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class LoginController implements Initializable {
	
	// ClientMain의 Stage를 얻는다(primaryStage), 다이얼로그의 소유자
	private Stage primaryStage;
	
	// TextField의 써 있는 id를 얻고, PasswordField의 써 있는 password를 얻는다
	private @FXML TextField id;
	private @FXML PasswordField password;
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 엔터 이벤트 로그인 시도
		id.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
					btnLogin();
				}
			}
		});
		
		// 엔터 이벤트 로그인 시도
		password.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				if (e.getCode().equals(KeyCode.ENTER)) {
					btnLogin();
				}
			}
		});
	}
	
	// 회원가입 화면을 연다
	// 회원가입 버튼 클릭 시 발생하는 이벤트
	public void btnJoin() throws IOException {
		// 회원가입 화면을 다이얼로그로 생성, 다이얼로그는 부 화면(ClientMain이 주 화면)
		Stage joinDialog = new Stage(StageStyle.UTILITY);
		joinDialog.initModality(Modality.WINDOW_MODAL);
		// 다이얼로그는 소유주를 설정해줘야 함
		joinDialog.initOwner(primaryStage);
		joinDialog.setTitle("회원가입");
		
		// Join.fxml의 디자인을 화면에 띄우기 위한 코드
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Join.fxml"));
		Parent parent = loader.load();
		
		// "회원가입 성공" 화면을 위해 "회원가입" 화면을 설정한다
		JoinController joinController = loader.getController();
		joinController.setPrimaryStage(joinDialog);
		
		Scene scene = new Scene(parent);
		joinDialog.setScene(scene);
		joinDialog.setResizable(false);
		joinDialog.setX(primaryStage.getX() + 50);
		joinDialog.setY(primaryStage.getY() + 50);
		
		joinDialog.show();
		
	}
	
	// id와 pw 정보를 서버에 넘겨서 서버의 파일에 존재하는지 확인한다.
	public void btnLogin() {
		// 서버와 통신하기 위해 소켓 생성
		Socket socket = null;
		try {
			socket = new Socket();
			// 서버에 주소와 포트에 연결 요청
			socket.connect(new InetSocketAddress("localhost", 5001));
			
			// id와 password를 얻어서 저장
			String joinId = id.getText();
			String joinPassword = password.getText();
			
			// 로그인을 알려주기위해 "Login"을 보내주고 '/'로 아이디와 비밀번호를 구분
			byte[] joinIdPassword = ("L/" + joinId + "/" + joinPassword).getBytes("UTF-8");
			// 서버에 아이디와 비밀번호를 전송
			OutputStream os = socket.getOutputStream();
			os.write(joinIdPassword);
			os.flush();
			
			// 서버에서 로그인 결과를 얻음 success or fail
			byte[] loginResult = new byte[10];
			InputStream is = socket.getInputStream();
			int readByte = is.read(loginResult);
			
			String result = new String(loginResult, 0, readByte, "utf-8");
			System.out.println("로그인 결과: " + result);
			
			if (result.equals("success")) {
				// 현재 로그인한 클라이언트 id와 pw, socket 정보를 설정한다
				Client client = Client.getClient();
				client.setData(joinId, joinPassword, socket);
				client.setFriends();
				client.start();
				
				client.setrooms();
				
				FXMLLoader loader = new FXMLLoader(getClass().getResource("Friends.fxml"));
				Parent root = loader.load();
				Stage newStage = new Stage();
				
				FriendsController friendsControl = loader.getController();
				friendsControl.setPrimaryStage(newStage);
				
				// UI 변경을 위해
				client.setFriendsControl(friendsControl);
				
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("css/friends.css").toString());
				
				friendsControl.setScene(scene);
				newStage.setScene(scene);
				newStage.setResizable(false);
				newStage.setOnCloseRequest(e -> logout());
				newStage.getIcons().add(new Image(getClass().getResource("images/icon.jpg").toString()));
				newStage.setTitle("마카오톡 ver 0.0");
				newStage.show();
			
				primaryStage.close();
				
			} else {
				throw new Exception(); // 로그인 실패
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			// 로그인 실패
			Stage loginFail = new Stage(StageStyle.UTILITY);
			loginFail.initModality(Modality.WINDOW_MODAL);
			loginFail.initOwner(primaryStage);
			
			Parent parent = null;
			try {
				parent = FXMLLoader.load(getClass().getResource("LoginFail.fxml"));
			} catch (IOException e1) {}
			
			// LoginFail에 있는 button의 이벤트 처리
			Button button = (Button)parent.lookup("#button");
			button.setOnAction(event -> loginFail.close());
			
			Scene scene = new Scene(parent);
			loginFail.setScene(scene);
			loginFail.setResizable(false);
			loginFail.setX(primaryStage.getX() + 50);
			loginFail.setY(primaryStage.getY() + 50);
			loginFail.show();
			
			try {
				socket.close();
			} catch (IOException e2) {}
		}
	}
	
	// 로그아웃
	public void logout() {
		Client client = Client.getClient();
		client.stop();
	}
}
