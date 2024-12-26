$(document).ready(function() {
    validateSection();
    checkIdAvailable();
    submit();
});

// 전역 변수
let isUsernameDuplicate = true;
const validators = {
    username: {
        regex: /^[A-Za-z0-9]{4,}$/,
        message: '아이디는 영어와 숫자로 4자 이상이어야 합니다.'
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



//아이디 중복값 체크
function checkIdAvailable() {
    $('#checkUsername').on('click', function() {
        const username = $('#username').val();

        if (!validateField('username', username)) {
            return;
        }

        $.ajax({
            url: '/api/checkUsername',
            method: 'POST',
            data: { username: username },
            success: function() {
                isUsernameDuplicate = false;
                $('#usernameError').text('사용 가능한 아이디입니다.').css('color', 'green');
            },
            error: function(xhr) {
                if (xhr.status === 409) { // 아이디가 중복된 경우 409 상태 코드 처리
                    isUsernameDuplicate = true;
                    $('#usernameError').text('이미 사용중인 아이디입니다.').css('color', '#e50914');
                } else { // 그 외의 알 수 없는 오류
                    alert("서버 오류가 발생했습니다. 다시 시도해주세요.");
                }
            }
        });
    });
}

//입력값 유효성 체크
function validateField(fieldId, value) {
    const validator = validators[fieldId];
    const $error = $(`#${fieldId}Error`);

    if (!validator.regex.test(value)) {
        $error.text(validator.message);
        return false;
    }
    $error.text('');
    return true;
}

//비밀번호 재확인 처리
function validatePasswordConfirm() {
    const password = $('#password').val();
    const passwordConfirm = $('#passwordConfirm').val();
    const $error = $('#passwordConfirmError');

    if (password !== passwordConfirm) {
        $error.text('비밀번호가 일치하지 않습니다.');
        return false;
    }
    $error.text('');
    return true;
}

//검증 처리 담당
function validateSection() {
    // 입력 필드 검증
    $('input').on('blur', function() {
        const fieldId = $(this).attr('id');
        const value = $(this).val();

        if (fieldId === 'passwordConfirm') {
            validatePasswordConfirm();
        } else if (validators[fieldId]) {
            validateField(fieldId, value);
        }
    });
}

// 회원가입 버튼
function submit() {
    $('.signupForm').on('submit', function(e) {
        e.preventDefault();

        const username = $('#username').val();
        const nickname = $('#nickname').val();
        const password = $('#password').val();

        // 아이디 중복확인 체크
        if (isUsernameDuplicate) {
            alert('아이디 중복확인을 해주세요.');
            $('#check-username').focus();
            return;
        }
        // 아이디 체크
        if (!validateField('username', username)) {
            alert('아이디는 영어와 숫자로 4자 이상이어야 합니다.');
            $('#username').focus();
            return;
        }
        // 닉네임 체크
        if (!validateField('nickname', nickname)) {
            alert('닉네임은 공백일 수 없습니다.');
            $('#nickname').focus();
            return;
        }
        // 비밀번호 체크
        if (!validateField('password', password)) {
            alert('비밀번호는 4자 이상이어야 합니다.');
            $('#password').focus();
            return;
        }
        // 비밀번호 확인 체크
        if (!validatePasswordConfirm()) {
            alert('비밀번호가 일치하지 않습니다.');
            $('#password-confirm').focus();
            return;
        }

        //서버로 회원가입 시도
        $.ajax({
            url: "/register",
            method: "post",
            data: {
                username: username,
                nickname: nickname,
                password: password,
            },
            success: function() {
                alert("회원가입이 완료 되었습니다. 다시 로그인 하세요.");
                window.location.href = "/login";
            },
            error: function(xhr) {
                console.log(xhr.responseText);
                alert("서버 오류가 발생했습니다. 다시 시도해주세요.");
            }
        });
    });
}
