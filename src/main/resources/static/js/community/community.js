
// 외부 함수 선언
const handleCategoryChange = (category) => {
    $('.categoryTab').removeClass('active');
    $(`.categoryTab[data-category="${category}"]`).addClass('active');


};



// 애니메이션 효과
const animatePostCards = () => {
    $('.postCard').each(function(index) {
        $(this).css({
            'animation': `fadeInUp 0.3s ease forwards ${index * 0.1}s`,
            'opacity': '0'
        });
    });
};

// 디바운스 함수
const debounce = (func, wait) => {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};

// 문서 로드 완료 시 실행
$(document).ready(function() {
    // 카테고리 탭 클릭 이벤트
    $('.categoryTab').on('click', function() {
        handleCategoryChange($(this).data('category'));
    });

    // 정렬
    $('.sortSelect').on('change', function() {

    });

    // 검색 입력 이벤트
    $('.searchBox input').on('input', debounce(function() {


    }, 300));


    // 글쓰기 버튼 클릭 이벤트
    $('.writeButton').on('click', function (){
        window.location.href = '/postWrite';
    });

    // 초기 게시글 로드 및 애니메이션
    animatePostCards();
});

// 키프레임 애니메이션 정의
$('<style>')
    .prop('type', 'text/css')
    .html(`
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    `)
    .appendTo('head');