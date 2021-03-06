import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Avatar,
  Button,
  List,
  ListItem,
  ListItemAvatar,
  ListItemButton,
  ListItemText,
  Typography,
} from '@mui/material';
import FollowButton from './FollowButton';
import { getFollowerList } from '../../services/api/FollowApi';
import { getFollowingList } from '../../services/api/FollowApi';
import { getLoggedUserId } from '../../utils/loggedUser';

/**
 * 팔로워, 팔로잉 리스트 공용
 * @param {listType, setOpenModal, listOwnerId}
 * listType: 'follower': 현재 프로필을 팔로우하고 있는 계정 리스트 / 'following': 현재 프로필이 팔로우하고 있는 계정 리스트
 * setOpenModal: props로 modifier함수 가져오기 (모달창 닫아야 함)
 * listOwnerId: 리스트의 주체 userId
 */
export default function FollowAccountList({ listType, setOpenModal, listOwnerId }) {
  const navigate = useNavigate();
  // 사용자 계정 목록
  const [accountList, setAccountList] = useState([]);
  const isFollowerList = listType === 'follower';

  const [triggerRefresh, setTriggerRefresh] = useState(true);

  const loggedUserId = getLoggedUserId();

  // const preventClick = (event) => {
  //   event.stopPropagation();
  //   event.preventDefault();
  // };

  const getAccountList = async () => {
    const list = isFollowerList
      ? await getFollowerList(loggedUserId, listOwnerId)
      : await getFollowingList(loggedUserId, listOwnerId);
    setAccountList(list);
  };

  useEffect(() => {
    getAccountList();
  }, [triggerRefresh]);

  /* 사용자 프로필을 클릭했을 시 해당 사용자의 프로필로 이동 */
  const handleAccountClick = (userId) => {
    // console.log(userId);
    // 아이템(사용자 계정)을 누르면 모달창을 닫는다
    setOpenModal(false);
    navigate(`/users/profile/${userId}`, { replace: true });
  };

  return (
    <List
      sx={{
        width: '100%',
        maxWidth: '100%',
        height: 650,
        overflow: 'auto',
        bgcolor: 'background.paper',
      }}
      // dense
    >
      {accountList.map((account, index) => {
        const labelId = `checkbox-list-secondary-label-${account}`;
        return (
          <ListItem
            key={index}
            secondaryAction={
              //? 사용자 본인일 경우에는 버튼을 생성하지 않음
              (isFollowerList && account.followingId !== loggedUserId) ||
              (!isFollowerList && account.followerId !== loggedUserId) ? (
                <FollowButton
                  isFollowButton={!account.isFollow}
                  // 리스트의 종류에 따라서, accountId를 각각 다르게 꺼내와야 함
                  accountId={isFollowerList ? account.followingId : account.followerId}
                  setTriggerRefresh={setTriggerRefresh}
                  // accountId={account.followingId}
                />
              ) : null
            }
            disablePadding
          >
            <ListItemButton
              onClick={() =>
                handleAccountClick(isFollowerList ? account.followingId : account.followerId)
              }
            >
              <ListItemAvatar>
                <Avatar
                  // todo 사용자 프로필 추가 + listType에 따라서 갈리게
                  alt={`Avatar n°${account.followingNickname + 1}`}
                  src={account.followUserProfilePhoto}
                />
              </ListItemAvatar>
              <ListItemText
                id={labelId}
                primary={
                  <Typography
                    sx={{ display: 'inline' }}
                    component="span"
                    variant="body1"
                    color="text.primary"
                  >
                    {isFollowerList ? account.followingNickname : account.followerNickname}
                  </Typography>
                }
                secondary={
                  <>
                    <Typography
                      sx={{ display: 'inline' }}
                      component="span"
                      variant="caption"
                      color="text.disabled"
                    >
                      @{isFollowerList ? account.followingId : account.followerId}
                    </Typography>
                  </>
                }
              />
            </ListItemButton>
          </ListItem>
        );
      })}
    </List>
  );
}
