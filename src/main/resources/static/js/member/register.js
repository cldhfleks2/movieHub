$(document).ready(function() {
    validateSection();
    checkIdAvaliable();
    submit()
});
//전역 변수
let isUsernameDuplicate = true;

function validateField(fieldId, value) {
    const validator = validators[fieldId];
    const $error = $(`#${fieldId}-error`);

    if (!validator.regex.test(value)) {
        $error.text(validator.message);
        return false;
    }
    $error.text('');
    return true;
}

function validatePasswordConfirm() {
    const password = $('#password').val();
    const passwordConfirm = $('#password-confirm').val();
    const $error = $('#password-confirm-error');

    if (password !== passwordConfirm) {
        $error.text('비밀번호가 일치하지 않습니다.');
        return false;
    }
    $error.text('');
    return true;
}

function validateSection(){
    const validators = {
        username: {
            regex: /^[a-zA-Z]{4,}$/,
            message: '아이디는 영문자로 4자 이상이어야 합니다.'
        },
        nickname: {
            regex: /\S+/,
            message: '닉네임은 공백일 수 없습니다.'
        },
        password: {
            regex: /.{4,}/,
            message: '비밀번호는 4자 이상이어야 합니다.'
        }
    };

    // 입력 필드 검증
    $('input').on('blur', function() {
        const fieldId = $(this).attr('id');
        const value = $(this).val();

        if (fieldId === 'password-confirm') {
            validatePasswordConfirm();
        } else if (validators[fieldId]) {
            validateField(fieldId, value);
        }
    });
}

function checkIdAvaliable(){
    // 아이디 중복 확인
    $('#check-username').on('click', function() {
        const username = $('#username').val();

        if (!validateField('username', username)) {
            return;
        }

        $.ajax({
            url: '/api/check-username',
            method: 'POST',
            data: { username: username },
            success: function(response) {
                isUsernameDuplicate = false;
                $('#username-error').text('사용 가능한 아이디입니다.').css('color', 'green');
            },
            error: function() {
                isUsernameDuplicate = true;
                $('#username-error').text('이미 사용중인 아이디입니다.').css('color', '#e50914');
            }
        });
    });
}

function submit(){
    // 폼 제출
    $('.signup-form').on('submit', function(e) {
        e.preventDefault();

        const username = $('#username').val();
        const nickname = $('#nickname').val();
        const password = $('#password').val();

        if (!validateField('username', username) ||
            !validateField('nickname', nickname) ||
            !validateField('password', password) ||
            !validatePasswordConfirm() ||
            isUsernameDuplicate) {
            return;
        }

        this.submit();
    });
}