package com.ssafy.pollar.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2" , strategy = "uuid2")
    @Column(length = 36)
    private String uid;

    @Column(length = 16 , name = "userId")
    private String userId;

    @Column(length = 16 , name = "userNickname")
    private String userNickname;

    @Column(length = 45 , name = "userEmail")
    private String userEmail;

    @Column(name = "userBirthday")
    private Date userBirthday;

    @Column(name = "userSex")
    private Boolean userSex;

    @Column(name = "userProfilePhoto" ,length = 1000)
    private String userProfilePhoto;

    @OneToMany(mappedBy = "follower")
    private List<Following> follower = new ArrayList<>();

    @OneToMany(mappedBy = "followee")
    private List<Following> followee = new ArrayList<>();

    @OneToMany(mappedBy = "userCategory")
    private List<userCategory> userCategorys = new ArrayList<>();

    @OneToMany(mappedBy = "userPariticipate")
    private List<voteParticipate> userParticipates = new ArrayList<>();

    @OneToMany(mappedBy = "userVoteLike")
    private  List<voteLike> userVoteLikes = new ArrayList<>();

    @OneToMany(mappedBy = "replyUser")
    private List<Reply> userReplys = new ArrayList<>();

    // user안에 update메소드를 만들어서 request를 param으로 바꿔서
    // setter => update

    private void update(User user){// builder와 사용 목적이 같다.
        this.uid = user.uid;
    }

}