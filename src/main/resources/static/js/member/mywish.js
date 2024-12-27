$(document).ready(function() {
    // View toggle functionality
    $('#gridView').click(function() {
        $(this).addClass('active');
        $('#listView').removeClass('active');
        $('#wishlistContent').removeClass('wishlistList').addClass('wishlistGrid');
    });

    $('#listView').click(function() {
        $(this).addClass('active');
        $('#gridView').removeClass('active');
        $('#wishlistContent').removeClass('wishlistGrid').addClass('wishlistList');
    });

    // Sort functionality
    $('#sortSelect').change(function() {
        const sortValue = $(this).val();
        const $wishlistContent = $('#wishlistContent');
        const $movies = $wishlistContent.children('.movieCard').get();

        $movies.sort(function(a, b) {
            switch(sortValue) {
                case 'latest':
                    return $(b).find('.movieYear').text() - $(a).find('.movieYear').text();
                case 'rating':
                    return $(b).find('.movieRating').text() - $(a).find('.movieRating').text();
                case 'title':
                    return $(a).find('.movieTitle').text().localeCompare($(b).find('.movieTitle').text());
                default:
                    return 0;
            }
        });

        $wishlistContent.empty().append($movies);
    });

    // Remove wish functionality
    $('.btnRemoveWish').click(function(e) {
        e.stopPropagation();
        const $movieCard = $(this).closest('.movieCard');

        // Add fade out animation
        $movieCard.fadeOut(300, function() {
            $(this).remove();
        });

        // Here you would typically make an AJAX call to your backend
        // to remove the movie from the user's wishlist
    });




    pagination();
});

function pagination(){
    // Pagination functionality
    $('.pageNumber').click(function() {
        $('.pageNumber').removeClass('active');
        $(this).addClass('active');

        // Here you would typically make an AJAX call to fetch the next page of results
        // and update the wishlist content
    });

    $('#prevPage, #nextPage').click(function() {
        const $activeButton = $('.pageNumber.active');
        let $targetButton;

        if ($(this).attr('id') === 'prevPage') {
            $targetButton = $activeButton.prev('.pageNumber');
        } else {
            $targetButton = $activeButton.next('.pageNumber');
        }

        if ($targetButton.length) {
            $targetButton.click();
        }
    });
}