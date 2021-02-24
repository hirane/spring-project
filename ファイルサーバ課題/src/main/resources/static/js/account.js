/**
 *
 */

//ユーザ名更新処理
document.getElementById('updateName').addEventListener('click', (e) => {
	e.preventDefault();
	document.updateForm.action = '/updateName';
	document.updateForm.submit();
});

//パスワード更新処理
document.getElementById('updatePw').addEventListener('click', (e) => {
	e.preventDefault();
	document.updateForm.action = '/updatePassword';
	document.updateForm.submit();
});

