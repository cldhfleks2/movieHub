// 상수 정의
const VIEWS = {
    CARD: 'card',
    LIST: 'list'
};

// 외부 함수 선언
const handleCategoryChange = (category) => {
    $('.categoryTab').removeClass('active');
    $(`.categoryTab[data-category="${category}"]`).addClass('active');


};

const handleViewChange = (view) => {
    $('.viewButton').removeClass('active');
    $(`.viewButton[data-view="${view}"]`).addClass('active');
    $('.postList').removeClass('cardView listView').addClass(`${view}View`);
};

const handlePostAction = (action, postId) => {
    switch(action) {
        case 'bookmark':
            console.log(`Bookmarking post: ${postId}`);
            break;
        case 'share':
            console.log(`Sharing post: ${postId}`);
            break;
        case 'like':
            console.log(`Liking post: ${postId}`);
            break;
    }
};

const handleWritePost = () => {
    window.location.href = '/community/write';
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

// 무한 스크롤 핸들러
const handleInfiniteScroll = () => {
    const scrollHeight = $(document).height();
    const scrollPosition = $(window).height() + $(window).scrollTop();

    if ((scrollHeight - scrollPosition) / scrollHeight === 0) {

    }
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

    // 뷰 토글 버튼 클릭 이벤트
    $('.viewButton').on('click', function() {
        handleViewChange($(this).data('view'));
    });

    // 정렬
    $('.sortSelect').on('change', function() {

    });

    // 검색 입력 이벤트
    $('.searchBox input').on('input', debounce(function() {


    }, 300));

    // 게시글 액션 버튼 클릭 이벤트
    $('.actionButton').on('click', function() {
        const action = $(this).data('action');
        const postId = $(this).closest('.postCard').data('post-id');
        handlePostAction(action, postId);
    });


    // 글쓰기 버튼 클릭 이벤트
    $('.writeButton').on('click', handleWritePost);

    // 무한 스크롤 이벤트
    $(window).on('scroll', debounce(handleInfiniteScroll, 100));

    // 초기 게시글 로드 및 애니메이션
    animatePostCards();

    // 반응형 처리
    $(window).on('resize', debounce(function() {
        if ($(window).width() < 768) {
            handleViewChange(VIEWS.LIST);
        }
    }, 250));
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