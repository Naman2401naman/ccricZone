package com.criczone.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

public final class ApiRequests {

    private ApiRequests() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Pattern(regexp = "^[0-9]{10,15}$", message = "must contain 10 to 15 digits")
        private String phone;
        @NotBlank
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoginRequest {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateProfileRequest {
        private String name;
        @Pattern(regexp = "^$|^[0-9]{10,15}$", message = "must contain 10 to 15 digits")
        private String phone;
        private Map<String, Object> profile;
        private Map<String, Object> media;
        private Map<String, Object> notifications;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Map<String, Object> getProfile() { return profile; }
        public void setProfile(Map<String, Object> profile) { this.profile = profile; }
        public Map<String, Object> getMedia() { return media; }
        public void setMedia(Map<String, Object> media) { this.media = media; }
        public Map<String, Object> getNotifications() { return notifications; }
        public void setNotifications(Map<String, Object> notifications) { this.notifications = notifications; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateRoleRequest {
        @NotBlank
        private String userId;
        @NotBlank
        private String role;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateBookingRequest {
        @NotBlank
        private String turfId;
        @NotBlank
        private String date;
        @NotBlank
        private String startTime;
        @NotBlank
        private String endTime;

        public String getTurfId() { return turfId; }
        public void setTurfId(String turfId) { this.turfId = turfId; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdatePaymentRequest {
        @NotBlank
        private String paymentStatus;
        private String paymentMethod;
        private String paymentReference;

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getPaymentReference() { return paymentReference; }
        public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateTurfRequest {
        @NotBlank
        private String turfName;
        @NotNull
        private Map<String, Object> location;
        private List<String> sportTypes;
        private Map<String, Object> turfSize;
        private String surfaceType;
        private Map<String, Object> amenities;
        private List<String> images;
        @Positive
        private Double basePricingPerSlot;

        public String getTurfName() { return turfName; }
        public void setTurfName(String turfName) { this.turfName = turfName; }
        public Map<String, Object> getLocation() { return location; }
        public void setLocation(Map<String, Object> location) { this.location = location; }
        public List<String> getSportTypes() { return sportTypes; }
        public void setSportTypes(List<String> sportTypes) { this.sportTypes = sportTypes; }
        public Map<String, Object> getTurfSize() { return turfSize; }
        public void setTurfSize(Map<String, Object> turfSize) { this.turfSize = turfSize; }
        public String getSurfaceType() { return surfaceType; }
        public void setSurfaceType(String surfaceType) { this.surfaceType = surfaceType; }
        public Map<String, Object> getAmenities() { return amenities; }
        public void setAmenities(Map<String, Object> amenities) { this.amenities = amenities; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public Double getBasePricingPerSlot() { return basePricingPerSlot; }
        public void setBasePricingPerSlot(Double basePricingPerSlot) { this.basePricingPerSlot = basePricingPerSlot; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateTurfRequest extends CreateTurfRequest {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NearbyTurfsRequest {
        @NotNull
        private Double latitude;
        @NotNull
        private Double longitude;
        private Double maxDistance;

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        public Double getMaxDistance() { return maxDistance; }
        public void setMaxDistance(Double maxDistance) { this.maxDistance = maxDistance; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateMatchRequest {
        @NotBlank
        private String matchName;
        private String matchType;
        private Integer customOvers;
        @NotBlank
        private String venue;
        @NotBlank
        private String matchDate;
        private String tournamentId;
        private String teamAName;
        private String teamAId;
        private List<Map<String, Object>> teamAPlayers;
        private String teamBName;
        private String teamBId;
        private List<Map<String, Object>> teamBPlayers;

        public String getMatchName() { return matchName; }
        public void setMatchName(String matchName) { this.matchName = matchName; }
        public String getMatchType() { return matchType; }
        public void setMatchType(String matchType) { this.matchType = matchType; }
        public Integer getCustomOvers() { return customOvers; }
        public void setCustomOvers(Integer customOvers) { this.customOvers = customOvers; }
        public String getVenue() { return venue; }
        public void setVenue(String venue) { this.venue = venue; }
        public String getMatchDate() { return matchDate; }
        public void setMatchDate(String matchDate) { this.matchDate = matchDate; }
        public String getTournamentId() { return tournamentId; }
        public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
        public String getTeamAName() { return teamAName; }
        public void setTeamAName(String teamAName) { this.teamAName = teamAName; }
        public String getTeamAId() { return teamAId; }
        public void setTeamAId(String teamAId) { this.teamAId = teamAId; }
        public List<Map<String, Object>> getTeamAPlayers() { return teamAPlayers; }
        public void setTeamAPlayers(List<Map<String, Object>> teamAPlayers) { this.teamAPlayers = teamAPlayers; }
        public String getTeamBName() { return teamBName; }
        public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
        public String getTeamBId() { return teamBId; }
        public void setTeamBId(String teamBId) { this.teamBId = teamBId; }
        public List<Map<String, Object>> getTeamBPlayers() { return teamBPlayers; }
        public void setTeamBPlayers(List<Map<String, Object>> teamBPlayers) { this.teamBPlayers = teamBPlayers; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossRequest {
        @NotBlank
        private String tossWinnerTeam;
        @NotBlank
        private String decision;

        public String getTossWinnerTeam() { return tossWinnerTeam; }
        public void setTossWinnerTeam(String tossWinnerTeam) { this.tossWinnerTeam = tossWinnerTeam; }
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoreRequest {
        private Integer runs;
        private Integer wickets;
        private String overs;
        private String batsmanName;
        private String batsmanId;
        private String nonStrikerName;
        private String nonStrikerId;
        private String bowlerName;
        private String bowlerId;
        private List<Map<String, Object>> ballEvents;

        public Integer getRuns() { return runs; }
        public void setRuns(Integer runs) { this.runs = runs; }
        public Integer getWickets() { return wickets; }
        public void setWickets(Integer wickets) { this.wickets = wickets; }
        public String getOvers() { return overs; }
        public void setOvers(String overs) { this.overs = overs; }
        public String getBatsmanName() { return batsmanName; }
        public void setBatsmanName(String batsmanName) { this.batsmanName = batsmanName; }
        public String getBatsmanId() { return batsmanId; }
        public void setBatsmanId(String batsmanId) { this.batsmanId = batsmanId; }
        public String getNonStrikerName() { return nonStrikerName; }
        public void setNonStrikerName(String nonStrikerName) { this.nonStrikerName = nonStrikerName; }
        public String getNonStrikerId() { return nonStrikerId; }
        public void setNonStrikerId(String nonStrikerId) { this.nonStrikerId = nonStrikerId; }
        public String getBowlerName() { return bowlerName; }
        public void setBowlerName(String bowlerName) { this.bowlerName = bowlerName; }
        public String getBowlerId() { return bowlerId; }
        public void setBowlerId(String bowlerId) { this.bowlerId = bowlerId; }
        public List<Map<String, Object>> getBallEvents() { return ballEvents; }
        public void setBallEvents(List<Map<String, Object>> ballEvents) { this.ballEvents = ballEvents; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompleteMatchRequest {
        private String winner;
        private String resultType;
        private Integer resultMargin;

        public String getWinner() { return winner; }
        public void setWinner(String winner) { this.winner = winner; }
        public String getResultType() { return resultType; }
        public void setResultType(String resultType) { this.resultType = resultType; }
        public Integer getResultMargin() { return resultMargin; }
        public void setResultMargin(Integer resultMargin) { this.resultMargin = resultMargin; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateTeamRequest {
        @NotBlank
        private String name;
        private List<Map<String, Object>> members;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Map<String, Object>> getMembers() { return members; }
        public void setMembers(List<Map<String, Object>> members) { this.members = members; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateTeamRequest {
        private String name;
        private List<Map<String, Object>> members;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<Map<String, Object>> getMembers() { return members; }
        public void setMembers(List<Map<String, Object>> members) { this.members = members; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RespondInvitationRequest {
        @NotBlank
        private String action;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RandomizeTeamsRequest {
        private List<Map<String, Object>> players;
        private String teamAName;
        private String teamBName;

        public List<Map<String, Object>> getPlayers() { return players; }
        public void setPlayers(List<Map<String, Object>> players) { this.players = players; }
        public String getTeamAName() { return teamAName; }
        public void setTeamAName(String teamAName) { this.teamAName = teamAName; }
        public String getTeamBName() { return teamBName; }
        public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateTournamentRequest {
        @NotBlank
        private String name;
        private String description;
        private String shortName;
        @NotBlank
        private String startDate;
        @NotBlank
        private String endDate;
        private String registrationDeadline;
        @NotBlank
        private String venue;
        private String format;
        private Integer customOvers;
        private String tournamentType;
        private Integer maxTeams;
        private Integer minPlayers;
        private Integer maxPlayers;
        private Object prizePool;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getRegistrationDeadline() { return registrationDeadline; }
        public void setRegistrationDeadline(String registrationDeadline) { this.registrationDeadline = registrationDeadline; }
        public String getVenue() { return venue; }
        public void setVenue(String venue) { this.venue = venue; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public Integer getCustomOvers() { return customOvers; }
        public void setCustomOvers(Integer customOvers) { this.customOvers = customOvers; }
        public String getTournamentType() { return tournamentType; }
        public void setTournamentType(String tournamentType) { this.tournamentType = tournamentType; }
        public Integer getMaxTeams() { return maxTeams; }
        public void setMaxTeams(Integer maxTeams) { this.maxTeams = maxTeams; }
        public Integer getMinPlayers() { return minPlayers; }
        public void setMinPlayers(Integer minPlayers) { this.minPlayers = minPlayers; }
        public Integer getMaxPlayers() { return maxPlayers; }
        public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }
        public Object getPrizePool() { return prizePool; }
        public void setPrizePool(Object prizePool) { this.prizePool = prizePool; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TournamentTeamRequest {
        @NotBlank
        private String teamId;
        private String teamName;
        private Object captain;
        private Object viceCaptain;
        private Object wicketkeeper;
        private Object coach;
        private List<Map<String, Object>> players;

        public String getTeamId() { return teamId; }
        public void setTeamId(String teamId) { this.teamId = teamId; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Object getCaptain() { return captain; }
        public void setCaptain(Object captain) { this.captain = captain; }
        public Object getViceCaptain() { return viceCaptain; }
        public void setViceCaptain(Object viceCaptain) { this.viceCaptain = viceCaptain; }
        public Object getWicketkeeper() { return wicketkeeper; }
        public void setWicketkeeper(Object wicketkeeper) { this.wicketkeeper = wicketkeeper; }
        public Object getCoach() { return coach; }
        public void setCoach(Object coach) { this.coach = coach; }
        public List<Map<String, Object>> getPlayers() { return players; }
        public void setPlayers(List<Map<String, Object>> players) { this.players = players; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateStandingsRequest {
        @NotBlank
        private String tournamentId;
        private List<Map<String, Object>> standings;

        public String getTournamentId() { return tournamentId; }
        public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
        public List<Map<String, Object>> getStandings() { return standings; }
        public void setStandings(List<Map<String, Object>> standings) { this.standings = standings; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateStatusRequest {
        @NotBlank
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreatePostRequest {
        @NotBlank
        private String content;
        private String imageUrl;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentRequest {
        @NotBlank
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
