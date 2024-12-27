package com.cldhfleks2.moviehub.bookmark;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
}
