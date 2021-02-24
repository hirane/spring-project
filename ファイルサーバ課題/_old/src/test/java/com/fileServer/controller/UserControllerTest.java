package com.fileServer.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fileServer.entity.UserForm;
import com.fileServer.entity.Users;
import com.fileServer.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	//MpckMvcオブジェクト
	@Autowired
	private MockMvc mockMvc;

	//モックオブジェクト
	@MockBean
	UserService userService;

	//テスト対象クラス
	@Autowired
	UserController userController;

	 /**
     * 準備(各テストケース実行前に実行する処理)
     */
		/*@Before
		public void setup() {
		    // MockMvcオブジェクトにテスト対象メソッドを設定
		    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		}*/

	//1-001
    @Test
    public void 正常系_moveToRegistForm_登録画面にModelAndViewを返す() throws Exception{

       // テスト対象メソッドを実行
        MvcResult result = mockMvc.perform(get("/regist")).andDo(print())
                    // HTTPステータスがOKであることを確認
				.andExpect(status().isOk())
				    // 次画面の遷移先がregist.htmlであることを確認
				.andExpect(view().name("regist"))
					// ModelオブジェクトのisRegisteredに設定される値が正しいことを確認
				.andExpect(model().attribute("isRegistered", false))
				    // Modelオブジェクトにエラーが無いことを確認
				.andExpect(model().hasNoErrors())
                .andReturn();

		// ModelオブジェクトのuserFormに設定される値が正しいことを確認
		UserForm resultForm = (UserForm) result.getModelAndView().getModel().get("userForm");

		//検証:ModelのuserFormの初期値
		assertEquals(resultForm.getUserId(),null);
		assertEquals(resultForm.getUserName(),null);
		assertEquals(resultForm.getPassword(),null);
		assertEquals(resultForm.getConPassword(),null);
    }

    //1-002
    @Test
    public void 正常系_userRegist_登録画面にModelAndViewを返す() throws Exception{
    	//リクエストで受け取るUserFormオブジェクトを作成
    	UserForm userForm = new UserForm();
    	userForm.setUserId("test@new");
    	userForm.setUserName("new");
    	userForm.setPassword("test");
    	userForm.setConPassword("test");

    	// テスト対象メソッドを実行
    	//POSTで/registへパラメータuserFormをリクエスト送信。
    	mockMvc.perform(post("/regist").flashAttr("userForm",userForm)).andDo(print())
    	// HTTPステータスがOKであることを確認
    	.andExpect(status().isOk())
    	// 次画面の遷移先がregist.htmlであることを確認
    	.andExpect(view().name("regist"))
    	// ModelオブジェクトのisRegisteredにtrueが設定されていることを確認
    	.andExpect(model().attribute("isRegistered", true))
    	// Modelオブジェクトにエラーが無いことを確認
    	.andExpect(model().hasNoErrors())
    	.andReturn();
    }

    //1-003
    @Test
    public void 異常系_userRegist_ユーザID重複あり_登録画面にModelAndViewを返す() throws Exception{
    	//リクエストで受け取るUserFormオブジェクトのテストデータ作成
    	UserForm userForm = new UserForm();
    	userForm.setUserId("test@test");
    	userForm.setUserName("none");
    	userForm.setPassword("none");
    	userForm.setConPassword("none");

    	// モック生成
    	when(userService.isDuplicatedUserId("test@test")).thenReturn(true);

    	// テスト対象メソッドを実行
    	//POSTで/registへパラメータuserFormをリクエスト送信。
    	mockMvc.perform(post("/regist").flashAttr("userForm",userForm)).andDo(print())
    	// HTTPステータスがOKであることを確認
    	.andExpect(status().isOk())
    	// 次画面の遷移先がregist.htmlであることを確認
    	.andExpect(view().name("regist"))
    	// ModelオブジェクトのisRegisteredにfalseが設定されていることを確認
    	.andExpect(model().attribute("isRegistered", false))
    	// ModelオブジェクトのisErrにtrueが設定されていることを確認
    	.andExpect(model().attribute("isErr", true))
    	// ModelオブジェクトのerrMsgに"このユーザIDは既に使用されています"が設定されていることを確認
    	.andExpect(model().attribute("errMsg", "このユーザIDは既に使用されています"))
    	// Modelオブジェクトにエラーが無いことを確認
    	.andExpect(model().hasNoErrors())
    	.andReturn();
    }

    //1-004
    @Test
    public void 異常系_userRegist_ユーザ名重複あり_登録画面にModelAndViewを返す() throws Exception{
    	//リクエストで受け取るUserFormオブジェクトを作成
    	UserForm userForm = new UserForm();
    	userForm.setUserId("none@none");
    	userForm.setUserName("test");	//重複
    	userForm.setPassword("none");
    	userForm.setConPassword("none");

    	// モック生成
    	when(userService.isDuplicatedUserName("test")).thenReturn(true);

    	// テスト対象メソッドを実行
    	//POSTで/registへパラメータuserFormをリクエスト送信。
    	mockMvc.perform(post("/regist").flashAttr("userForm",userForm)).andDo(print())
    	// HTTPステータスがOKであることを確認
    	.andExpect(status().isOk())
    	// 次画面の遷移先がregist.htmlであることを確認
    	.andExpect(view().name("regist"))
    	// ModelオブジェクトのisRegisteredにfalseが設定されていることを確認
    	.andExpect(model().attribute("isRegistered", false))
    	// ModelオブジェクトのisErrにtrueが設定されていることを確認
    	.andExpect(model().attribute("isErr", true))
    	// ModelオブジェクトのerrMsgに"このユーザ名は既に使用されています"が設定されていることを確認
    	.andExpect(model().attribute("errMsg", "このユーザ名は既に使用されています"))
    	// Modelオブジェクトにエラーが無いことを確認
    	.andExpect(model().hasNoErrors())
    	.andReturn();
    }

    //1-005
    @Test
    public void 異常系_userRegist_パスワード不一致_登録画面にModelAndViewを返す() throws Exception{
    	//リクエストで受け取るUserFormオブジェクトを作成
    	UserForm userForm = new UserForm();
    	userForm.setUserId("none@none");
    	userForm.setUserName("none");	//重複
    	userForm.setPassword("test");
    	userForm.setConPassword("testtest");

    	// テスト対象メソッドを実行
    	//POSTで/registへパラメータuserFormをリクエスト送信。
    	mockMvc.perform(post("/regist").flashAttr("userForm",userForm)).andDo(print())
    	// HTTPステータスがOKであることを確認
    	.andExpect(status().isOk())
    	// 次画面の遷移先がregist.htmlであることを確認
    	.andExpect(view().name("regist"))
    	// ModelオブジェクトのisRegisteredにfalseが設定されていることを確認
    	.andExpect(model().attribute("isRegistered", false))
    	// ModelオブジェクトのisErrにtrueが設定されていることを確認
    	.andExpect(model().attribute("isErr", true))
    	// ModelオブジェクトのerrMsgに"パスワードが一致しません"が設定されていることを確認
    	.andExpect(model().attribute("errMsg", "パスワードが一致しません"))
    	// Modelオブジェクトにエラーが無いことを確認
    	.andExpect(model().hasNoErrors())
    	.andReturn();
    }

  //3-001
  	@Test
  	public void 正常系_showUserList_条件なし_usersリストをユーザ管理画面に返す() throws Exception{
  		//userServiceクラスのfindAllメソッド戻り値リストを作成
  		List<Users> list = new ArrayList<Users>();
  		//テストユーザ１を作成しリストに格納
  		Users user1 = new Users();
  		user1.setUserId("a@a");
    	user1.setUserName("aaa");
    	user1.setPassword("$2a$10$SzDMldqATmsKY4.oFKgh4eNnS5s5C9D73C9aRRwtwz4Z7xqlePWJ6");
    	user1.setAuthority(0);
  		list.add(user1);
  		//テストユーザ２を作成しリストに格納
  		Users user2 = new Users();
  		user2.setUserId("test@test");
  		user2.setUserName("test");
  		user2.setPassword("$2a$10$z6nLQNHJ4Ym.OnXeb.rUweiWV3UDkbfuvOdWZycKr4PyayEvGY8Fa");
  		user2.setAuthority(2);
  		list.add(user2);

  		//モック作成
  		when(userService.findAll()).thenReturn(list);

  		//テスト対象の処理実行
  		MvcResult result = mockMvc.perform(get("/userList")).andDo(print())
                // HTTPステータスがOKであることを確認
			.andExpect(status().isOk())
			    // 次画面の遷移先がuserList.htmlであることを確認
			.andExpect(view().name("userList"))
			    // Modelオブジェクトにエラーが無いことを確認
			.andExpect(model().hasNoErrors())
            .andReturn();

  		// ModelオブジェクトのuserFormに設定される値が正しいことを確認
  		List<Users> resultlist = (List<Users>)result.getModelAndView().getModel().get("userlist");

  		//検証
  		assertEquals(resultlist,list);
  	}

  	//3-003
  	@Test
  	public void 正常系_editAuthority_条件なし_showUserListの処理に遷移() throws Exception{
  		//引数の値をセット
  		String updateId = "test@test";
  		String newAuthority = "1";

  		//テスト対象の処理実行
  		mockMvc.perform(post("/editAuth")
  				.param("updateId",updateId)
  				.param("editAuth",newAuthority))
  				.andDo(print())
  				// HTTPステータスがOKであることを確認
  				.andExpect(status().isOk())
				// 次画面の遷移先がnullであることを確認
				.andExpect(view().name("userList"))
  				// Modelオブジェクトにエラーが無いことを確認
  				.andExpect(model().hasNoErrors())
  				.andReturn();
  	}

  	//3-005
  	@Test
  	public void 正常系_deleteUser_条件なし_showUserListの処理に遷移() throws Exception{
  		//引数の値をセット
  		String deleteId = "test@test";

  		//テスト対象の処理実行
  		mockMvc.perform(post("/delete")
  				.param("deleteId",deleteId))
  		.andDo(print())
  		// HTTPステータスがOKであることを確認
  		.andExpect(status().isOk())
  		// 次画面の遷移先がuserList.htmlであることを確認
  		.andExpect(view().name("userList"))
  		// modelオブジェクトのsuccessMsgに"削除しました"が設定されていることを確認
  		.andExpect(model().attribute("successMsg","削除しました"))
  		// Modelオブジェクトにエラーが無いことを確認
  		.andExpect(model().hasNoErrors())
  		.andReturn();
  	}

}



