package com.fileServer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fileServer.mapper.FileMapper;


/*
class ModelMap {
	public Object getAttribute(String attributeName) {
		return null;
	}

	public LinkedHashMap<String, Object> asMap() {
		return null;
	}
}
*/


@SpringBootTest
public class FileServiceTest {
/*
	// モック化するクラスのインスタンスを生成します
	@Mock
	private Model modelMock;

	// モックを注入するクラスのインスタンスを生成します。
	@InjectMocks
	private FileService fileServiceMock;

	// this(fileMapperMock)を初期化します。
	@SuppressWarnings("deprecation")
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
*/
//	private MockMvc mockMvc;
//	@MockBean
//	Model modelMock;

	@Autowired
	FileService fileServiceTest;
	@Autowired
	FileMapper fileMapperTest;
//	@Autowired
//	FileData fileDataTest;

//	@Before
//	public void setup() {
//		mockMvc = MockMvcBuilders.standaloneSetup(fileServiceTest).build();
//	}

	private MockMvc mockMvc;




	/*
	@Test
	public void 正常系_makeDirectory_まだ存在しないファイルパスがインプット_フォルダを作成する() {
		final String INPUT_PATH = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1";
		fileServiceTest.makeDirectory(INPUT_PATH);
	}
	*/

	//異常系は、想定として、パスが""やnull、ありえないドライブネームの場合など
	@Test
	public void 正常系_makeDirectory_存在するファイルのパスがインプット_存在するファイル情報でDBを更新() {
		final String INPUT_PATH = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1";
		//ファイルを作成して、SQLで削除・書き込みを行い、ファイルの有無の初期設定を自動化しないと無意味か
		fileServiceTest.makeDirectory(INPUT_PATH);
	}

	/*
	@Test
	public void 正常系_allFilesTest_ファイルのパスがインプット_存在するファイル情報をリストで返す() {
		final File INPUT_PATH = new File("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1");
		List<FileData> emptyList = new ArrayList<>();
		List<FileData> actualList = fileServiceTest.allFiles(INPUT_PATH, emptyList);
		List<FileData> expectedList = new ArrayList<>();

		//以下、filesテーブルに登録されている順番にすべて記述
		FileData fileData = new FileData();
		fileData.setFileName("test2");
		fileData.setFilePath("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2");
		fileData.setCreateDate("Unknown Date");
		fileData.setCreateUser("Unknown User");
		fileData.setUpdateDate("Unknown Date");
		fileData.setUpdateUser("Unknown User");
		expectedList.add(fileData);

		fileData = new FileData();
		fileData.setFileName("NotOnDB");
		fileData.setFilePath("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2\\NotOnDB");
		fileData.setCreateDate("Unknown Date");
		fileData.setCreateUser("Unknown User");
		fileData.setUpdateDate("Unknown Date");
		fileData.setUpdateUser("Unknown User");
		expectedList.add(fileData);

		fileData = new FileData();
		fileData.setFileName("OnDB");
		fileData.setFilePath("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2\\OnDB");
		fileData.setCreateDate("2020/12/24 10:30");
		fileData.setCreateUser("作成者");
		fileData.setUpdateDate("2020/12/24 10:30");
		fileData.setUpdateUser("更新者");
		expectedList.add(fileData);

		fileData = new FileData();
		fileData.setFileName("sampleNotOnDB.txt");
		fileData.setFilePath("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2\\sampleNotOnDB.txt");
		fileData.setCreateDate("Unknown Date");
		fileData.setCreateUser("Unknown User");
		fileData.setUpdateDate("Unknown Date");
		fileData.setUpdateUser("Unknown User");
		expectedList.add(fileData);

		fileData = new FileData();
		fileData.setFileName("sampleOnDB.txt");
		fileData.setFilePath("C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2\\sampleOnDB.txt");
		fileData.setCreateDate("2020/12/24 10:30");
		fileData.setCreateUser("作成者");
		fileData.setUpdateDate("2020/12/24 10:30");
		fileData.setUpdateUser("更新者");
		expectedList.add(fileData);

		assertEquals(expectedList, actualList);
	}
	*/


//	@Test
//	public void 正常系_dispFileList_2階層目以下のフォルダパスがインプット_modelに情報が入っている() {
		/*
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
//		Model inputModel = new Model();
//		final String INPUT_PATH = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer\\test1\\test2";
//
//		String holeDirectoryValue = "C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\FileServer";
//
//
//		fileServiceTest.dispFileList(INPUT_PATH, modelMock);
//		modelMock.asMap().entrySet().stream().forEach(s -> System.out.println(s));
//		String actual1 = (String) model.asMap().get("homeDirectory");
//		List<Object> list = new ArrayList<>(model.asMap().values());
//		String s = (String)model.asMap().get("homeDirectory");
		//
//		assertEquals(holeDirectoryValue, (String)modelMock.asMap().get("homeDirectory"));



//	}





//	@Test
//	public void 正常系_dispFileList_ホームフォルダパスがインプット_modelに情報が入っている() {
//
//	}





}
