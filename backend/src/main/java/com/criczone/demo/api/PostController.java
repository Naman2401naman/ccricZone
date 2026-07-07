package com.criczone.demo.api;

import com.criczone.demo.domain.UserDocument;
import com.criczone.demo.dto.ApiRequests.CommentRequest;
import com.criczone.demo.dto.ApiRequests.CreatePostRequest;
import com.criczone.demo.dto.RequestMaps;
import com.criczone.demo.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Social feed posts, likes, comments, and user feeds")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Map<String, Object> all() {
        return postService.all();
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> byUser(@PathVariable String userId) {
        return postService.byUser(userId);
    }

    @PostMapping
    public Map<String, Object> create(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @Valid @RequestBody CreatePostRequest request) {
        return postService.create(currentUser, RequestMaps.toMap(request));
    }

    @PostMapping("/{postId}/like")
    public Map<String, Object> like(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                    @PathVariable String postId) {
        return postService.like(currentUser, postId);
    }

    @PostMapping("/{postId}/comment")
    public Map<String, Object> comment(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                       @PathVariable String postId,
                                       @Valid @RequestBody CommentRequest request) {
        return postService.comment(currentUser, postId, RequestMaps.toMap(request));
    }

    @DeleteMapping("/{postId}")
    public Map<String, Object> delete(@RequestAttribute(value = "currentUser", required = false) UserDocument currentUser,
                                      @PathVariable String postId) {
        return postService.delete(currentUser, postId);
    }
}
