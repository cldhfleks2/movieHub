$(document).ready(function() {
    validation();
});

function validation() {
    const validators = {
        username: {
            regex: /^[a-zA-Z]{4,}$/,
            message: '아이디는 영문자로 4자 이상이어야 합니다.'
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

        if (validators[fieldId]) {
            validateField(fieldId, value);
        }
    });

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

    // 폼 제출
    $('.loginForm').on('submit', function(e) {
        e.preventDefault();

        const username = $('#username').val();
        const password = $('#password').val();

        if (!validateField('username', username) ||
            !validateField('password', password)) {
            return;
        }

        this.submit(); //제출
    });
}
