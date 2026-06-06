package com.cyberblog.backend.service;

public interface LikeService {
    boolean toggleLike(Long userId, String guestId, Long targetId, String targetType);
}
