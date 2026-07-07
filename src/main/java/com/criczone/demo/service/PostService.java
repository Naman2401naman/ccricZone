package com.criczone.demo.service;

import com.criczone.demo.config.CacheNames;
import com.criczone.demo.domain.PostDocument;
import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.repo.PostRepository;
import com.criczone.demo.support.ApiException;
import com.criczone.demo.support.ApiSupport;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Service;
@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    @Cacheable(CacheNames.POST_LIST)
    public Map<String, Object> all() {
        List<PostDocument> posts = postRepository.findAll().stream().sorted(Comparator.comparing(PostDocument::getCreatedAt).reversed()).collect(Collectors.toList());
        return Map.of("success", true, "count", posts.size(), "posts", posts);
    }

    @GetMapping("/user/{userId}")
    @Cacheable(cacheNames = CacheNames.POST_BY_USER, key = "#userId")
    public Map<String, Object> byUser(@PathVariable String userId) {
        List<PostDocument> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return Map.of("success", true, "count", posts.size(), "posts", posts);
    }

    @PostMapping
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.POST_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.POST_BY_USER, allEntries = true)
    })
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        PostDocument post = new PostDocument();
        post.setUserId(user.getId());
        post.setContent(ApiSupport.trim(request.get("content")));
        post.setImageUrl(ApiSupport.trim(request.get("imageUrl")));
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        return Map.of("success", true, "message", "Post created successfully", "post", post);
    }

    @PostMapping("/{postId}/like")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.POST_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.POST_BY_USER, allEntries = true)
    })
    public Map<String, Object> like(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String postId) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        PostDocument post = postRepository.findById(postId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        List<String> likes = new ArrayList<>(post.getLikes());
        if (likes.contains(user.getId())) {
            likes.remove(user.getId());
        } else {
            likes.add(user.getId());
        }
        post.setLikes(likes);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        return Map.of("success", true, "message", "Post like updated", "post", post);
    }

    @PostMapping("/{postId}/comment")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.POST_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.POST_BY_USER, allEntries = true)
    })
    public Map<String, Object> comment(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String postId,
                                       Map<String, Object> request) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        PostDocument post = postRepository.findById(postId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        post.getComments().add(Map.of("userId", user.getId(), "text", ApiSupport.trim(request.get("text")), "timestamp", Instant.now().toString()));
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
        return Map.of("success", true, "message", "Comment added successfully", "post", post);
    }

    @DeleteMapping("/{postId}")
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.POST_LIST, allEntries = true),
        @CacheEvict(cacheNames = CacheNames.POST_BY_USER, allEntries = true)
    })
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser, @PathVariable String postId) {
        UserDocument user = ApiSupport.requireUser(currentUser);
        PostDocument post = postRepository.findById(postId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!Objects.equals(post.getUserId(), user.getId()) && !"admin".equalsIgnoreCase(user.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        postRepository.delete(post);
        return Map.of("success", true, "message", "Post deleted successfully");
    }
}



