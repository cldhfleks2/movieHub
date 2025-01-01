$(document).ready(function() {
    getUserData();
    initialize();
    initializeImagePreview();
    initializeFormValidation();
    initializeEditMode();
    filterAndSearchSection();
    initializePostManagement();
    postPagination();

    reviewSection();
    reviewFilter();
    reviewEdit();
    reviewDelete();
    reviewPagination()
});

function initialize() {
    //탭을 클릭하면 active를 붙여줌
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

    //마이페이지오면 가장처음으로 개인 정보 수정 뷰 보여줌
    $("#postSection").hide();
    $("#reviewSection").hide();
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
    const $img = $("#profilePreview");
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
            $img.prop("disabled", false)
            $passwordFields.slideDown();
            $editOverlay.show(); // 수정 모드에서만 표시
            $editButton.text('수정완료');
            $cancelButton.show();
            $("#nickname").focus()
        } else {
            // 폼 제출
            submitForm();
        }
    });

    $cancelButton.on('click', function() {
        // 수정 모드 비활성화 및 원래 데이터로 복원
        isEditMode = false;
        $inputs.prop('disabled', true);
        $img.prop("disabled", true)
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
    const nickname = $('#nickname').val().trim();
    const password = $('#password').val().trim();
    const passwordConfirm = $('#passwordConfirm').val().trim();
    
    // 닉네임 검증
    if (!validateField('nickname', nickname)) {
        alert('닉네임을 확인해주세요.');
        $('#nickname').focus();
        return;
    }
    // 비밀번호 입력했는가
    if (password.length === 0) { // 공백이 제거된 비밀번호가 빈 문자열인지 확인
        alert('비밀번호를 입력해주세요.');
        $('#password').focus();
        return;
    }
    // 비밀번호 확인 입력 했는가
    if (passwordConfirm.length === 0) { 
        alert('비밀번호 확인을 입력해주세요.');
        $('#passwordConfirm').focus();
        return;
    }
    // 비밀번호 검증 : 4자리이상
    if (!validateField('password', password)) {
        alert('비밀번호를 확인해주세요.');
        $('#password').focus();
        return;
    }
    // 비밀번호 확인 검증 : 서로 같은가
    if (!validatePasswordConfirm()) {
        alert('비밀번호가 일치하지 않습니다.');
        $('#passwordConfirm').focus();
        return;
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
        url: '/api/user/profile/edit',
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function (response, textStatus, xhr){
            if (xhr.status === 200) {
                alert('프로필이 성공적으로 업데이트되었습니다.');
                location.reload();
            }
            console.log("/api/user/profile/edit ajax success")
        },
        error: function (xhr){
            alert('프로필 업데이트에 실패했습니다. 다시 시도해주세요.');
            console.log(xhr.responseText);
            console.log("/api/user/profile/edit ajax failed")
        }
    });
}
//카테고리선택/검색기능
function filterAndSearchSection() {
    // 필터 변경 이벤트
    $(document).on('change', '#categoryTabs, #sortTabs', function() {
        postListReload();
    });

    // 검색 이벤트
    $(document).on('input', '#postSearchInput', function() {
        postListReload();
    });

    // 검색 버튼
    $(document).on('click', '#postSearchBtn', function() {
        postListReload();
    });
}
//TODO 게시글 수정
function initializePostManagement() {
    // 게시글 수정 버튼 이벤트
    $('.editButton').on('click', function() {
        const postId = $(this).closest('.postCard').data('postId');


    });

    // 게시글 삭제 버튼 이벤트
    $('.deleteButton').on('click', function() {
        const postId = $(this).closest('.postCard').data('postId');


    });
}
//게시물, 페이지번호 뷰를 새로고침하는 코드 : pageIdx와 searchText(자동적용)
function postListReload(pageIdx = 1){
    const category = $("#categoryTabs").val(); //ALL, FREE, NEWS, DISCUSSION
    const sort = $("#sortTabs").val() //latest, view, like, review
    let keyword = $("#postSearchInput").val();

    $.ajax({
        url: "/mypage",
        method: "get",
        data: { pageIdx: pageIdx, keyword: keyword, category: category, sort: sort },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#postListContainer").replaceWith(dataHtml.find("#postListContainer"));
            $("#postPagination").replaceWith(dataHtml.find("#postPagination"));
            console.log("/mypage page-reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/mypage page-reload ajax failed")
        }
    });
}
//게시글 페이지네이션 : 페이지버튼들 동작
function postPagination() {
    $(document).on("click", "#postPrevPage, #postNextPage, .pageNumber", function () {
        const pageIdx = $(this).data("pageidx")
        postListReload(pageIdx)
    })
}

//댓글 뷰 : 댓글 뷰만 가져오는 함수
function reviewListReload(pageIdx = 1){
    const sort = $("#reviewSortFilter").val() //latest, like
    let keyword = $("#reviewSearchInput").val();

    $.ajax({
        url: "/mypage/review",
        method: "get",
        data: { pageIdx: pageIdx, keyword: keyword, sort: sort },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#reviewListContainer").replaceWith(dataHtml.find("#reviewListContainer"));
            $("#reviewPagination").replaceWith(dataHtml.find("#reviewPagination"));
            //페이지네이션 버튼도 replaceWith하도록 추가
            console.log("/mypage/review page-reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/mypage/review page-reload ajax failed")
        }
    });
}

// 댓글뷰 탭 선택하면 페이지를 가져옴
function reviewSection() {
    $(document).on("click", "#reviewTab", function () {
        reviewListReload();
    });
}

//정렬선택/검색함수
function reviewFilter() {
    //정렬 선택시
    $('#reviewSortFilter').on('change', function() {
        reviewListReload()
    });

    //검색 버튼 클릭시
    $('#reviewSearchBtn').on('click', function() {
        reviewListReload()
    });
    //검색어를 입력시
    $('#reviewSearchInput').on('input', function() {
        reviewListReload()
    });
}

// 댓글 뷰 : 댓글 수정 요청
function reviewEdit() {
    $('.reviewCard .editButton').on('click', function() {
        const reviewId = $(this).data('review-id');


    });
}

// 댓글 뷰 : 댓글 삭제 요청
function reviewDelete() {
    $('.reviewCard .deleteButton').on('click', function() {
        const reviewId = $(this).data('review-id');

    });
}

// 댓글 뷰 : 페이지네이션
function reviewPagination() {
    $(document).on("click", "#reviewPrevPage, #reviewNextPage, .pageNumber", function () {
        const pageIdx = $(this).data("pageidx")
        reviewListReload(pageIdx)
    })
}


