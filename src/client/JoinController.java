package client;

// 회원가입 설정

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class JoinController implements Initializable {
	
	private Stage primaryStage; // LoginController의 joinDialog
	
	private Socket socket;
	
	// TextField에서 id를 얻고 PasswordField에서 패스워드를 얻는다
	// 회원가입 할 id와 pw
	private @FXML TextField id;
	private @FXML PasswordField password;
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {}
	
	// id와 pw를 서버에 넘겨준다 
	// 서버가 파일 형태로 아이디와 비밀번호를 저장한다
	public void btnSubmit() {
		try {
		// 소켓 생성
		socket = new Socket();			
		socket.connect(new InetSocketAddress("localhost", 5001));
		
		// id와 pw를 얻어 String 변수에 저장
		String joinId = id.getText();
		String joinPassword = password.getText();
		
		// 통신을 위해 바이트 배열로 변환, 네트워크 통신은 바이트 단위
		// 회원가입 "J(oin)"를 보내 회원가입을 알리고 '/'로 아이디와 패스워드를 구분한다
		byte[] joinIdPassword = ("J/" + joinId + "/" + joinPassword).getBytes("UTF-8");
					
		OutputStream os = socket.getOutputStream();
		os.write(joinIdPassword);
		os.flush();
					
		// 회원가입 성공 화면
		Stage notice = new Stage(StageStyle.TRANSPARENT);
		notice.initModality(Modality.WINDOW_MODAL);
		notice.initOwner(primaryStage);
		notice.setTitle("회원가입 성공");
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("JoinSuccess.fxml"));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {}
		
		JoinSuccessController success = loader.getController();
		success.setPrimaryStage(notice);
		
		Scene scene = new Scene(root);
		notice.setScene(scene);
		notice.setX(primaryStage.getX() + 50);
		notice.setY(primaryStage.getY() + 50);
		notice.show();
		
		socket.close();
		} catch(Exception e) {
			// 회원가입 실패, 서버가 안열려 있거나 서버의 주소를 잘못 입력
			System.out.println("회원가입 실패");
			try {
				socket.close();
			} catch (IOException e1) {}
		}
		
	}
	
	// 회원가입 취소
	public void btnClose(ActionEvent event) {
		// 현재 화면 종료
		primaryStage.close();
	}
}
