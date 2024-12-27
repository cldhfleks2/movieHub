$(document).ready(function() {
    getUserData();
    initializeTabs();
    initializeImagePreview();
    initializeFormValidation();
    initializeEditMode();
});

//탭을 클릭하면 active를 붙여줌
function initializeTabs() {
    $('.tabButton').on('click', function() {
        const tabId = $(this).data('tab');

        //현재 선택한 탭 하이라이트
        //모든 탭에서 active클래스를 제거하고 현재 선택한 탭만 active추가
        $('.tabButton').removeClass('active');
        $(this).addClass('active');

        //모든 섹션의 active클래스를 제거하고 현재 선택한 섹션만 active추가
        $('.contentSection').removeClass('active');
        $(`#${tabId}Section`).addClass('active');

        //모든 섹션을 숨기고 현재 선택한 섹션만 보여준다
        $(".contentSection").hide();
        $(`#${tabId}Section`).show();
    });
}
// 원본 데이터 저장용
let originalData = {};
//개인 정보 : 서버에서 가져온 값을 기록해둠
function getUserData(){
    originalData = {
        username: $('#username').val(),
        nickname: $('#nickname').val(),
        profileImage: $('#profilePreview').attr('src') // 예시 이미지
    };
}
//개인 정보 : 수정하기 버튼 눌렀을때 수정 기능을 제공
function initializeEditMode() {
    let isEditMode = false;
    const $editButton = $('#editButton');
    const $cancelButton = $('<button type="button" class="cancelButton">수정 취소</button>').hide();
    const $inputs = $('#profileForm input').not('#username');
    const $passwordFields = $('.passwordFields');
    const $editOverlay = $('.editOverlay');
    $editOverlay.hide(); // 처음에는 숨김

    // 버튼 컨테이너 생성 및 버튼 추가
    const $buttonContainer = $('<div class="buttonContainer"></div>');
    $editButton.after($buttonContainer);
    $buttonContainer.append($editButton, $cancelButton);

    $editButton.on('click', function() {
        if (!isEditMode) {
            // 수정 모드 활성화
            isEditMode = true;
            $inputs.prop('disabled', false);
            $passwordFields.slideDown();
            $editOverlay.show(); // 수정 모드에서만 표시
            $editButton.text('수정완료');
            $cancelButton.show();
        } else {
            // 폼 제출
            submitForm();
        }
    });

    $cancelButton.on('click', function() {
        // 수정 모드 비활성화 및 원래 데이터로 복원
        isEditMode = false;
        $inputs.prop('disabled', true);
        $passwordFields.slideUp();
        $editOverlay.hide();
        $('.imageUpload').hide();
        $editButton.text('수정하기');
        $cancelButton.hide();

        // 원래 데이터로 복원
        $('#username').val(originalData.username);
        $('#nickname').val(originalData.nickname);
        $('#profilePreview').attr('src', originalData.profileImage);
        $('#password').val('');
        $('#passwordConfirm').val('');

        // 에러 메시지 초기화
        $('.errorMessage').removeClass('show');
    });
}
//개인 정보 : 비밀번호 검증
function initializeFormValidation() {
    const validators = {
        nickname: {
            regex: /\S+/,
            message: '닉네임은 공백일 수 없습니다.'
        },
        password: {
            regex: /.{4,}/,
            message: '비밀번호는 4자 이상이어야 합니다.'
        }
    };

    // 입력 필드 blur 이벤트 처리
    $('input').on('blur', function() {
        if ($(this).prop('disabled')) return;

        const fieldId = $(this).attr('id');
        const value = $(this).val();

        if (fieldId === 'passwordConfirm') {
            validatePasswordConfirm();
        } else if (validators[fieldId]) {
            validateField(fieldId, value);
        }
    });

    // 실시간 비밀번호 확인 체크
    $('#passwordConfirm').on('input', function() {
        if (!$(this).prop('disabled')) {
            validatePasswordConfirm();
        }
    });
}
//개인 정보 : 입력란 검증에 따라 에러메시지 표시
function validateField(fieldId, value) {
    const validators = {
        nickname: {
            regex: /\S+/,
            message: '닉네임은 공백일 수 없습니다.'
        },
        password: {
            regex: /.{4,}/,
            message: '비밀번호는 4자 이상이어야 합니다.'
        }
    };

    const validator = validators[fieldId];
    const $error = $(`#${fieldId}Error`);

    if (!validator.regex.test(value)) {
        $error.text(validator.message).addClass('show');
        return false;
    }
    $error.removeClass('show');
    return true;
}
//개인 정보 : 비밀번호 재확인 검증
function validatePasswordConfirm() {
    const password = $('#password').val();
    const passwordConfirm = $('#passwordConfirm').val();
    const $error = $('#passwordConfirmError');

    if (password !== passwordConfirm) {
        $error.text('비밀번호가 일치하지 않습니다.').addClass('show');
        return false;
    }
    $error.removeClass('show');
    return true;
}
//개인 정보 : 이미지 업로드
function initializeImagePreview() {
    // 프로필 이미지를 클릭하면 파일 선택창 열기
    $('#profilePreview').on('click', function() {
        if (!$('#profileImage').prop('disabled')) {
            $('#profileImage').click();
        }
    });

    // 파일 선택 시 미리보기 업데이트
    $('#profileImage').on('change', function(e) {
        if (e.target.files && e.target.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#profilePreview').attr('src', e.target.result);
            };
            reader.readAsDataURL(e.target.files[0]);
        }
    });

    // 편집 모드에서만 오버레이 클릭 가능
    $('.editOverlay').on('click', function() {
        if (!$('#profileImage').prop('disabled')) {
            $('#profileImage').click();
        }
    });
}
//개인 정보 : 수정 완료
function submitForm() {
    const nickname = $('#nickname').val();
    const password = $('#password').val();

    // Validate nickname
    if (!validateField('nickname', nickname)) {
        alert('닉네임을 확인해주세요.');
        $('#nickname').focus();
        return;
    }

    // Validate password if it's being changed
    if (password) {
        if (!validateField('password', password)) {
            alert('비밀번호를 확인해주세요.');
            $('#password').focus();
            return;
        }

        if (!validatePasswordConfirm()) {
            alert('비밀번호가 일치하지 않습니다.');
            $('#passwordConfirm').focus();
            return;
        }
    }

    const formData = new FormData();
    formData.append('nickname', nickname);
    if (password) {
        formData.append('password', password);
    }

    const profileImage = $('#profileImage')[0].files[0];
    if (profileImage) {
        formData.append('profileImage', profileImage);
    }

    $.ajax({
        url: '/api/user/profile',
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function() {
            alert('프로필이 성공적으로 업데이트되었습니다.');
            location.reload();
        },
        error: function(xhr) {
            alert('프로필 업데이트에 실패했습니다. 다시 시도해주세요.');
        }
    });
}