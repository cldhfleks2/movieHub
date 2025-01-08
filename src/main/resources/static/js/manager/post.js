$(document).ready(function() {
    initialize();
    initialSearching();
    pagination();
    searchingBar();
    editBtn();
    savePostChanges();
    deletePost();
    editCancelBtn();
});

// 페이지 초기화 시 필요한 설정들
function initialize() {
    searching(); //최소 1회 검색

    hidePostDetail();

    //페이지가 길어지면 그에따라 사이드바 높이도 설정
    $(document).on("transitionend", ".container", function () {
        const currentContainerHeight = $(".container").outerHeight();
        $(".adminSideBar").css("height", `${currentContainerHeight}px`);
    });
}

//최초 검색 동작 : 신고 관리 페이지에서 자세히 보기 버튼 클릭시
function initialSearching(){
    // URL에서 initialKeyword 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const postId  = urlParams.get('postId');

    if (postId) {
        searchingPostDetail(postId);
    }
}

//검색바 동작
function searchingBar() {
    // 이벤트 리스너 설정
    $(document).on("input", "#searchInput", function () {
        searching();
    });
}
//게시글 상세 뷰 보여주기
function showPostDetail() {
    $('#postDetailSection').show();
    // AJAX: 게시글 상세 정보 로드
}
//게시글 상세 뷰 숨김
function hidePostDetail() {
    $('#postDetailSection').hide();
}

//게시글 검색해서 결과를 보여주는 함수
function searching(pageIdx = 1) {
    hidePostDetail();
    const keyword = $("#searchInput").val();
    $.ajax({
        url: "/api/manager/post/search",
        method: "get",
        data: {pageIdx: pageIdx, keyword: keyword},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#searchResultBody").replaceWith(dataHtml.find("#searchResultBody"));
            $("#pagination").replaceWith(dataHtml.find("#pagination"));

            console.log("search-post ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("search-post ajax failed")
        }

    })
}
//검색결과 에서 수정하기 버튼 눌렀을때 게시글 상세를 보여줌
function editBtn(){
    $(document).on("click", ".editBtn", function () {
        const postId = $(this).data("post-id");
        searchingPostDetail(postId);
    });
}

//게시글 상세 정보 뷰를 가져오는 ajax함수
function searchingPostDetail(postId){
    $.ajax({
        url: "/api/manager/post/detail",
        method: "get",
        data: {postId: postId},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#postDetailSection").replaceWith(dataHtml.find("#postDetailSection"));
            showPostDetail();

            // 스크롤 이동
            $('html, body').animate({
                scrollTop: $('#postDetailSection').offset().top
            }, 500);

            console.log("searching-postDetail ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("searching-postDetail ajax failed")
        }
    })
}
//게시글 검색결과 페이지네이션
function pagination() {
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        searching(pageIdx)
        // 스크롤 이동
        $('html, body').animate({
            scrollTop: $('#searchInput').offset().top
        }, 10);
    })
}
//게시글 상세뷰에서 수정내용 저장
function savePostChanges() {
    // AJAX: 게시글 수정 저장
    $(document).on("click", ".saveBtn", function () {
        var formData = new FormData();
        const postId = $("#postDetailForm #postId").val();
        const postType = $("#postDetailForm #detailPostType").val();
        const title = $("#postDetailForm #detailTitle").val();
        const content = $("#postDetailForm #detailContent").val();
        formData.append('postId', postId);      // postId
        formData.append('postType', postType);  // 게시판 종류
        formData.append('title', title);        // 제목
        formData.append('content', content);    // 내용

        $.ajax({
            url: "/api/manager/post/edit",
            method: "patch",
            data: formData,
            processData: false,
            contentType: false,
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) {
                    //검색 섹션으로 화면 이동
                    $('html, body').animate({
                        scrollTop: $('.searchSection').offset().top
                    }, 300);
                    
                    hidePostDetail();

                    searching(); //검색결과 리셋

                    alert("게시글이 저장 되었습니다.")
                }
                console.log("edit-post ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("edit-post ajax failed")
            }
        })
    });
}
//게시글 상세뷰에서 게시글 삭제
function deletePost() {
    $(document).on("click", ".deleteBtn", function () {
        if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
            const postId = $("#postDetailForm #postId").val();

            // AJAX: 게시글 삭제
            $.ajax({
                url: "/api/manager/post/delete",
                method: "delete",
                data: {postId: postId},
                success: function (response, textStatus, xhr){
                    if (xhr.status === 204) {
                        alert("게시글이 삭제 되었습니다.")
                    }
                    console.log("delete-post ajax success")
                },
                error: function (xhr){
                    console.log(xhr.responseText);
                    console.log("delete-post ajax failed")
                }
            })
        }
    });
}
//게시글 수정 취소 버튼
function editCancelBtn(){
    $(document).on("click", ".cancelBtn", function () {
        //검색 섹션으로 화면 이동
        $('html, body').animate({
            scrollTop: $('.searchSection').offset().top
        }, 300);

        hidePostDetail();
    });
}
