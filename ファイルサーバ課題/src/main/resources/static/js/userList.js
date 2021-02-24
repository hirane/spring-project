/**
 *
 */

//削除・権限更新の対象とするユーザID
const userId = document.getElementById('userId');
//変更後の権限の番号(1 or 2)
const editAuth = document.getElementById('editAuth');
//送信フォーム
const form = document.getElementById('userForm');

//権限変更時の処理
const changeUserAuth = (id, authority) => {
	userId.value = id;
	editAuth.value = authority;
	form.action = "/editAuth"
	document.userListForm.submit();
}

//削除時の処理
const deleteUser = (id, authority) => {
	if (confirm('ユーザを削除します。よろしいですか。')) {
		userId.value = id;
		editAuth.value = authority;
		form.action = "/delete"
		document.userListForm.submit();
		return true;
	} else {
		return false;
	}
}


