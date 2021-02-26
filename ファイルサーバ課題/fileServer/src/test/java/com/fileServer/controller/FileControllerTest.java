package com.fileServer.controller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.ModelAndView;

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.FileData;
import com.fileServer.entity.Users;

/*
@Setter
@Getter
class DbUsersDetails {
	private int auth0 = 0;
	private int auth1 = 1;
	private int auth2 = 2;
}

@Getter
@Setter
class Model {
	public void addAttribute(String attributeName, Object attributeValue) {
		// TODO 自動生成されたメソッド・スタブ
//		LinkedHashMap<String, Object> modelList = new LinkedHashMap<>();
		map.put(attributeName, attributeValue);
//		return modelList;
	}
	private LinkedHashMap<String, Object> map;
}
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@AutoConfigureMockMvc
//@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileControllerTest {

//	/*
	// モック化するクラスのインスタンスを生成します。
//	@Mock
//	private FileService fileServiceMock;
//	@Mock
//	private Model modelMock;
	@Mock
	private DbUsersDetails loginUserMock;

	// モックを注入するクラスのインスタンスを生成します。
	@InjectMocks
	private FileController fc = new FileController();

	// this(fileServiceMock)を初期化します。
	@SuppressWarnings("deprecation")
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
//	 */

	//
	//	@MockBean
	//	FileService fileServiceMock;
/*
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private Users users;
	@Autowired
	private FileData fileData;

//
//
	@MockBean
	FileService fileServiceMock;
//
	@Autowired
	FileController fileControllerTest;
	//	@Autowired
	//	Model model;
	//		@Autowired
	//		DbUsersDetails loginUser;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.standaloneSetup(fileControllerTest).build();
	}
*/

	/*
	@Mock
	private FileMapper fileMapperMock;
	@Mock
	private Model model;
	@Mock
	private DbUsersDetails authority;

	@InjectMocks
	private FileService fs;
	@InjectMocks
	private FileController fc;

	@SuppressWarnings("deprecation")
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	*/

	//		Model model;
	//		DbUsersDetails user;
	@Test
	public void 正常系_toMoveView_2階層目以下のパスが入力されたとき_modelに情報を詰めてfileViewを返す() throws Exception {
		final String INPUT_PATH = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1";
		Users users = new Users();
		FileData fileData = new FileData();

		users.setAuthority(0);

//		when(loginUserMock).thenReturn(2);


		String homeDirectory = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer";

		ModelAndView actualResult = fc.moveToFileView(null, INPUT_PATH, loginUserMock);
		assertEquals("fileView", actualResult);


		//		DbUsersDetails user = new DbUsersDetails();
		//		Model modelList = new Model();
		/*
		//インターフェイスクラスのため、中身の定義はなし
		//空の状態だと、デバッグコンソール上では
		Model model = new Model() {

			@Override
			public Model mergeAttributes(Map<String, ?> attributes) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public Object getAttribute(String attributeName) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public boolean containsAttribute(String attributeName) {
				// TODO 自動生成されたメソッド・スタブ
				return false;
			}

			@Override
			public Map<String, Object> asMap() {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public Model addAttribute(String attributeName, Object attributeValue) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public Model addAttribute(Object attributeValue) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public Model addAllAttributes(Map<String, ?> attributes) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}

			@Override
			public Model addAllAttributes(Collection<?> attributeValues) {
				// TODO 自動生成されたメソッド・スタブ
				return null;
			}
		};
		 */


//		final String INPUT_PATH = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1";
		//期待値処理一覧
		/*filesテーブル登録順(再帰処理される順番)、●はUnknownでない
		 *
		 * homeNotOnDB
		 * homeOnDB●
		 * test1●
		 * test2●
		 * NotOnDB
		 * OnDB●
		 * test3
		 * sampleNotOnDB.txt
		 * sampleOnDB.txt●
		 * testDirSample.txt
		 * homeSampleNotOnDB.txt
		 * homeSampleOnDB.txt●
		 *
		 * */
//		String homeDirectory = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer";
		//実際の処理結果一覧
		//		String homeDirectoryResult = (String) mvcResult.getModelAndView().getModel().get("homeDirectory");

		//				String str = fileControllerTest.toMoveView(INPUT_PATH, modelList, user);
		//		model.asMap().entrySet().stream().forEach(s -> System.out.println(s));
		//		assertEquals("fileView", str);

		/*
		MvcResult mvcResult = mockMvc.perform(post("/fileView").param("directoryPath", INPUT_PATH))
				.andExpect(status().isOk())
				.andExpect(view().name("fileView"))
				.andReturn();
		 */

		//		assertEquals(homeDirectory, homeDirectoryResult);

		//		model.asMap().entrySet().stream().forEach(s -> System.out.println(s));


		//		this.mockMvc.perform(post("/fileView").param("directoryPath", INPUT_PATH))
		//		.andExpect(status().isOk())
		//		.andExpect(view().name("fileView"))
		//		.andExpect(model().attribute("homeDirectory", homeDirectory));

//		String str = fileControllerTest.toMoveView(INPUT_PATH, model, user);
//		assertEquals("fileView", str);


		/*
		MvcResult result = mockMvc.perform(post("/fileView").param("directoryPath", INPUT_PATH))
				.andExpect(status().isOk())
				.andExpect(view().name("fileView"))
				.andReturn();

		String actualResult = (String) result.getModelAndView().getModel().get("homeDirectory");
		System.out.println(actualResult);
		*/

//		when(fc.toMoveView(INPUT_PATH, model, authority)).thenReturn
//		String actual = FileController.toMoveView(INPUT_PATH, model, authority);

//		Users user = new Users();
//		FileData fileData = new FileData();



	}

}
