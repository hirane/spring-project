/**
 *
 */


const backBtn = document.getElementById('backBtn');
const registBtn = document.getElementById('registBtn');
const registForm = document.getElementById('registForm');

registBtn.addEventListener('click', (e) => {
	e.preventDefault();
	registForm.action = '/regist';
	registForm.method = 'POST';
	document.registForm.submit();
});


backBtn.addEventListener('click', (e) => {
	e.preventDefault();
	registForm.action = '/login';
	registForm.method = 'GET';
	document.registForm.submit();
});


