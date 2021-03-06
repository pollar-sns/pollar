package com.ssafy.pollar.model.service;

import com.ssafy.pollar.domain.entity.Category;
import com.ssafy.pollar.domain.entity.User;
import com.ssafy.pollar.domain.entity.UserCategory;
import com.ssafy.pollar.domain.entity.UserNotificationState;
import com.ssafy.pollar.model.dto.UserDto;
import com.ssafy.pollar.model.repository.CategoryRepository;
import com.ssafy.pollar.model.repository.UserNotificationStateRepository;
import com.ssafy.pollar.model.repository.UserRepository;
import com.ssafy.pollar.model.repository.UserCategoryRepository;
import com.ssafy.pollar.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final UserNotificationStateRepository userNotificationStateRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

//    @Value("${custom.path.upload-images}")
//    @Value("${file.path}")
//    private String uploadFolder;

    @Override
    public void signup(UserDto userDto) throws Exception {

        User user = User.builder()
                .userId(userDto.getUserId())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .userNickname(userDto.getUserNickname())
                .userEmail(userDto.getUserEmail())
                .userBirthday(userDto.getUserBirthday())
                .userGender(userDto.getUserGender())
                .build();
        // User??? user ?????? ??????
        userRepository.save(user);
        // User ?????? ??????
        userNotificationStateRepository.save(UserNotificationState.builder().userId(user)
                .allNotificationState(true)
                .followNotificationState(true)
                .feedNotificationState(true)
                .build());
        // User??? ????????? ??????????????? Id??? ????????? User Id??? ?????? UserCategory??? ??????
       for (long categoryId : userDto.getCategories()){
            Category category = categoryRepository.findById(categoryId).get();
            userCategoryRepository.save(UserCategory.builder()
                    .userCategory(user)
                    .category(category)
                    .build());
        }
    }

    @Override
    public boolean idCheck(String userId) throws Exception {
        if(userRepository.findByUserId(userId).isPresent()){//???????????? ????????????
            return false;
        }else{//???????????? ????????????
            return true;
        }
    }

    @Override
    public boolean nicknameCheck(String userNickname) throws Exception {
        if(userRepository.findByUserNickname(userNickname).isPresent()){//???????????? ????????????
            return false;
        }else{//???????????? ????????????
            return true;
        }
    }

    @Override
    public boolean emailCheck(String userEmail) throws Exception {
        if(userRepository.findByUserEmail(userEmail).isPresent()){//???????????? ????????????
            return false;
        }else{//???????????? ????????????
            return true;
        }
    }

    @Override
    public boolean passwordCheck(String userId,String userPassword) throws Exception {
        if(userRepository.findByUserIdAndPassword(userId,userPassword).isPresent()){//???????????? ???????????????
            return true;
        }else{
            return false;
        }
    }

    //???????????? ??????
    @Override
    public void modifyUserInfo(UserDto userDto) throws Exception{
        User usercur = userRepository.findByUserId(userDto.getUserId()).get();

        usercur.nickNameUpdate(userDto.getUserNickname());
        // User??? user ?????? ??????
        userRepository.save(usercur);
    }

    @Override
    public void deleteUserInfo(String userId) throws Exception {
        Optional<User> user = userRepository.findByUserId(userId);

        userRepository.delete(user.get());
    }

    @Override
    public boolean login(UserDto userDto) throws Exception {
        if(userDto.getUserId() == null || userDto.getPassword()==null){
            return false;
        }else{
            String dbPassword = getPassword(userDto.getUserId());
            if(!passwordEncoder.matches(userDto.getPassword(),dbPassword)){
                return false;
            }else{
                return true;
            }
        }

    }

    @Override
    public String modifyProfile(UserDto userDto, MultipartFile userProfilePhoto) throws Exception {
        UUID uuid = UUID.randomUUID();

        User usercur = userRepository.findByUserId(userDto.getUserId()).get();
        // ?????? I/O
        try {
//            Files.write(imageFilePath, userProfilePhoto.getBytes());
//            s3Uploader.upload(userProfilePhoto, "profile");
            String imgPath = s3Uploader.upload(userProfilePhoto, "profile");
            System.out.println("==============================================");
            System.out.println("????????? s3 ?????? : " + imgPath);
            System.out.println("==============================================");
            // db??? id ??????????????? ????????? ?????????
            User user = User.builder()
                    .uid(usercur.getUid())
                    .userId(usercur.getUserId())
                    .password(usercur.getPassword())
                    .userNickname((usercur.getUserNickname()))
                    .userEmail((usercur.getUserEmail()))
                    .userBirthday((usercur.getUserBirthday()))
                    .userGender((usercur.getUserGender()))
                    .userProfilePhoto(imgPath)
                    .build();

            userRepository.save(user);
            return imgPath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void ListFile( String strDirPath ) {
        File path = new File( strDirPath );
        File[] fList = path.listFiles();
        for( int i = 0; i < fList.length; i++ ) {
            if( fList[i].isFile() ) {
                System.out.println( fList[i].getPath() );
                // ????????? FullPath ??????
            } else if( fList[i].isDirectory() ) {
                ListFile( fList[i].getPath() ); // ???????????? ??????
            }
        }
    }


    @Override
    public String findId(String userEmail) throws Exception {
        String userId = userRepository.findByUserEmail(userEmail).get().getUserId();
        return userId;
    }

    // ???????????? ??????
    @Transactional
    @Override
    public void modifyPassword(UserDto userDto) throws Exception {
        User user = userRepository.findByUserId(userDto.getUserId()).get();
        user.passwordUpdate(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
    }

    @Override
    public UserDto getUserInfo(String userId) throws Exception {
        User user = userRepository.findByUserId(userId).get();
        UserDto userDto = new UserDto(user);
        return userDto;
    }

    @Override
    public String getPassword(String userId) throws Exception {
        User user = userRepository.findByUserId(userId).get();
        String password = user.getPassword();
        return password;
    }

}
