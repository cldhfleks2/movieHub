$(document).ready(function (){
    clickMenuBar();
    checkbox();
    displayContainer();
    postViewBtns();
})

function clickMenuBar(){
    $(document).on("click", ".menuItem", function () {
        $(".menuItem").removeClass("active"); //모든 active제거
        $(this).addClass("active"); //현재 활성화한것에만 active추가
    });
}

function displayContainer(){
    $(document).on("click", "#postMenu", function (){
        $("#postContainer").show()
        $("#reviewContainer").hide()
    })
    $(document).on("click", "#reviewMenu", function (){
        $("#postContainer").hide()
        $("#reviewContainer").show()
    })
}

function checkbox() {
    const $selectAllRowsCheckbox = $('.selectAllRows');
    const $rowCheckboxes = $('.row-checkbox');

    // 전체 선택/해제 기능
    $selectAllRowsCheckbox.on('change', function() {
        $rowCheckboxes.prop('checked', this.checked);
    });

    // 개별 선택 시 전체 선택 체크박스 상태 업데이트
    $rowCheckboxes.on('change', function() {
        const allChecked = $rowCheckboxes.length === $rowCheckboxes.filter(':checked').length;
        $selectAllRowsCheckbox.prop('checked', allChecked);
    });
}
//페이지 새로고침
function postViewReload(){
    //ajax 코드
}

function postViewBtns(){
    // 선택 제외 버튼
    $(document).on("click", ".accept-btn", function () {
        const selectedReportIds = $('.postTableBody .row-checkbox:checked').map(function() {
            return $(this).data('report-id');
        }).get();

        if (selectedReportIds.length > 0) {
            //ajax코드, 성공시 postViewReload()
        } else {
            alert('선택된 항목이 없습니다.');
        }
    });

    // 선택 삭제 버튼
    $(document).on("click", ".reject-btn", function () {
        const selectedReportIds = $('.postTableBody .row-checkbox:checked').map(function() {
            return $(this).data('report-id');
        }).get();

        if (selectedReportIds.length > 0) {
            //ajax코드, 성공시 postViewReload()
        } else {
            alert('선택된 항목이 없습니다.');
        }
    });
}


