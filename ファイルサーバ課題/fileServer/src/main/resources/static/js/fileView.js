
//――――――――――――――――アップロード――――――――――――――――――――――――――――//

const multipartFile = document.getElementById('multipartFile');
const droppable = document.getElementById('droppable');
const contentsWrap = document.getElementById('contentsWrap');
const isValid = e => e.dataTransfer.types.indexOf('Files') >= 0;

//ボタンからファイルを選択してアップロードした場合、fileUpload関数へ
multipartFile.addEventListener('change', (e) => {
	fileUpload();
});

//全体にドラッグオーバー禁止
contentsWrap.addEventListener('dragover', (e) => {
	e.stopPropagation();
	e.preventDefault();
	e.dataTransfer.dropEffect = "none"; return;
});

//範囲内にドラッグオーバー時に、範囲に着色
droppable.addEventListener('dragover', (e) => {
	e.stopPropagation();
	e.preventDefault();

	if (!isValid(e)) {
		//無効なデータがドラッグされたらドロップを無効にする
		e.dataTransfer.dropEffect = "none"; return;
	} else {
		droppable.classList.add('dragOver');
	}
});

//範囲外へドラッグ状態を抜ける
droppable.addEventListener('dragleave', (e) => {
	e.stopPropagation();
	e.preventDefault();
	droppable.classList.remove('dragOver');
});

//ドロップしたときに、正常ファイルであればアップロードされる
//ファイルとフォルダを分けるために、一般的でないファイル形式で0バイトデータはアップロードできない仕様になっています
droppable.addEventListener('drop', (e) => {
	e.stopPropagation();
	e.preventDefault();
	droppable.classList.remove('dragOver');
	var files = e.dataTransfer.files
	multipartFile.files = files;

	let isIncludeDirectory = false;
	//不明な(一般的でない)ファイル形式で0バイトデータの場合ははじく
	for (let i = 0; i < files.length; i++) {
		if ((files[i].type == '' && files[i].size == 0) || files[i].type == '' || files[i].type == null) {
			alert('対応していない形式が含まれています');
			isIncludeDirectory = true;
			break;
		}
	}
	if (isIncludeDirectory == false) {
		fileUpload();
	}
});


//アップロード時の同名ファイル競合処理
//該当フォルダにあるファイル一覧の情報
const fileNameList = document.querySelectorAll('.fileNameLabel');
//	window.addEventListener('DOMContentLoaded', function(e) {
//ファイルが選択されたときにイベント発火
//指定されたファイルを取得
const uploadFileList = document.querySelector('#multipartFile');
const fileUpload = () => {
	//アップロードされたファイル数カウント
	let uploadFileNum = 0;
	//同名ファイル数カウント
	let sameNameCount = 0;
	//アップロードされたファイル数分ループ
	while (document.querySelector('#multipartFile').files[uploadFileNum] != null) {
		let input = uploadFileList.files[uploadFileNum];
		//ファイル数分同名か確認
		for (let j = 0; j < fileNameList.length; j++) {
			if (input.name.toUpperCase() === fileNameList[j].textContent.toUpperCase()) {
				sameNameCount++;
				break;
			}
		}
		uploadFileNum++;
	} //while文ここまで

	//同名ファイルが存在しなかった場合
	if (sameNameCount == 0) {
		//form送信
		document.getElementById('isExistSameName').value = "notExist";
		document.uploadForm.submit();
	} else {
		//同名ファイルが存在するが、上書きする場合
		Swal.fire({
			html: sameNameCount + " / " + uploadFileNum + "件の同名ファイルが存在します。上書きしますか。",
			icon: 'warning',
			showCancelButton: true,
			cancelButtonColor: '#F44',
			confirmButtonText: '上書きします',
			cancelButtonText: 'キャンセル',
		}).then((result) => {
			if (result.isConfirmed) {
				//form送信
				document.getElementById('isExistSameName').value = "exist";
				document.uploadForm.submit();
			}
		})
	}
};

//――――――――――――――――ダウンロード――――――――――――――――――――――――――――//

const download = document.getElementById('download');
//テーブル内のダウンロードからダウンロードフォーム送信
download.addEventListener('click', (e) => {
	document.downloadForm.submit();
});

const dlFile = (form) => {
	form.submit();
	return false;
}

const dlDirectory = (form) => {
	form.submit();
	return false;
}

//――――――――――――――――フォルダ作成――――――――――――――――――――――――――――//

const checkboxOfMkdir = document.getElementById('checkboxOfMkdir');
const mkdirTextValue = document.getElementById('mkdirTextValue');
const popupMkdirSubmitBtn = document.getElementById('popupMkdirSubmitBtn');
const mkdirThisDirectory = document.getElementById('mkdirThisDirectory');
const newDirectoryPath = document.getElementById('newDirectoryPath');
//フォルダ作成ボタン押下時、新規フォルダ名入力ポップアップ表示
checkboxOfMkdir.addEventListener('change', (e) => {
	const name = "新しいフォルダ";
	//		checkboxOfMkdir.checked = true;
	//画面上からクリックする場合
	mkdir(name, false);
});

const mkdir = (fileName, bool) => {
	//エラーの場合はロード時に表示する
	if (bool) {
		checkboxOfMkdir.checked = true;
	}
	if (mkdirTextValue.value == "") {
		mkdirTextValue.value = fileName;
	}
	mkdirTextValue.select();
}

popupMkdirSubmitBtn.addEventListener('click', (e) => {
	e.preventDefault();
	if (mkdirThisDirectory.value.includes('/')) {
		newDirectoryPath.value = mkdirThisDirectory.value + '/' + mkdirTextValue.value;
	} else {
		newDirectoryPath.value = mkdirThisDirectory.value + '\\' + mkdirTextValue.value;
	}
	document.mkdirForm.submit();
});


//フォルダ作成ポップアップウインドウ閉じたらエラーメッセージ消す
checkboxOfMkdir.addEventListener('change', (e) => {
	if (document.getElementById('mkdirErrMsgs')) {
		document.getElementById('mkdirErrMsgs').style.display = "none";
	}
});

//――――――――――――――――名称変更――――――――――――――――――――――――――――//

const checkboxOfRename = document.getElementById('checkboxOfRename');
const popupRenameSubmitBtn = document.getElementById('popupRenameSubmitBtn');
const afterPath = document.getElementById('afterPath');
const renameThisDirectory = document.getElementById('renameThisDirectory');
const renameTextValue = document.getElementById('renameTextValue');
const renameTargetPath = document.getElementById('renameTargetPath');
//名称変更ボタン押下時、ファイル名変更ポップアップ表示
const rename = (filePath, fileName) => {
	renameTextValue.value = fileName;
	renameTextValue.select();
	renameTargetPath.value = filePath;
	checkboxOfRename.checked = true;
}

popupRenameSubmitBtn.addEventListener('click', (e) => {
	e.preventDefault();
	if (renameThisDirectory.value.includes('/')) {
		afterPath.value = renameThisDirectory.value + '/' + renameTextValue.value;
	} else {
		afterPath.value = renameThisDirectory.value + '\\' + renameTextValue.value;
	}
	document.changeNameForm.submit();
});

//名称変更ポップアップウインドウ閉じたらエラーメッセージ消す
checkboxOfRename.addEventListener('change', (e) => {
	if (document.getElementById('renameErrMsgs')) {
		document.getElementById('renameErrMsgs').style.display = "none";
	}
});

//――――――――――――――――移動――――――――――――――――――――――――――――//

const checkboxOfRenameByMove = document.getElementById('checkboxOfRenameByMove');
const popupMoveSubmitBtn = document.getElementById('popupMoveSubmitBtn');
const departure = document.getElementById('departure');
const destinationFilePath = document.getElementById('destinationFilePath');
var departureName;
var destinationPath;
//移動ボタン押下時、フォルダ一覧ポップアップ表示
const dirMove = (filePath) => {
	checkboxOfRenameByMove.checked = true;
	popupMoveSubmitBtn.disabled = true;
	popupMoveSubmitBtn.style.cursor = "not-allowed";
	departure.value = filePath;
	departureName = filePath.replace(/^.*[\\\/]/, '');
}

//移動先フォルダ選択時、ボタン押下可能に
const isCheckedRadio = (destination) => {
	popupMoveSubmitBtn.disabled = false;
	popupMoveSubmitBtn.style.cursor = "pointer";
	destinationPath = destination;
}

popupMoveSubmitBtn.addEventListener('click', (e) => {
	e.preventDefault();
	if (destinationPath.includes('/')) {
		destinationFilePath.value = destinationPath + '/' + departureName;
	} else {
		destinationFilePath.value = destinationPath + '\\' + departureName;
	}
	document.renameByMoveForm.submit();
});

//移動先選択ポップアップウインドウ閉じたらエラーメッセージ消す
checkboxOfRenameByMove.addEventListener('change', (e) => {
	if (document.getElementById('moveErrMsgs')) {
		document.getElementById('moveErrMsgs').style.display = "none";
	}
});

//――――――――――――――――削除――――――――――――――――――――――――――――//

const deleteTarget = document.getElementById('deleteTarget');
//フォルダ削除時の処理
const deleteDirectory = (filePath, fileName) => {
	Swal.fire({
		html: "[ " + fileName + " ]内全てを削除します。よろしいですか。",
		icon: 'warning',
		showCancelButton: true,
		cancelButtonColor: '#F44',
		confirmButtonText: '削除します',
		cancelButtonText: 'キャンセル',
	}).then((result) => {
		if (result.isConfirmed) {
			//form送信
			deleteTarget.value = filePath;
			document.deleteForm.submit();
			return true;
		} else {
			return false;
		}
	})
}

//ファイル削除時の処理
const deleteFile = (filePath, fileName) => {
	Swal.fire({
		html: "[ " + fileName + " ]を削除します。よろしいですか。",
		icon: 'warning',
		showCancelButton: true,
		cancelButtonColor: '#F44',
		confirmButtonText: '削除します',
		cancelButtonText: 'キャンセル',
	}).then((result) => {
		if (result.isConfirmed) {
			//form送信
			deleteTarget.value = filePath;
			document.deleteForm.submit();
			return true;
		} else {
			return false;
		}
	})
}



