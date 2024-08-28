package com.example.Angle.SecurityTests.ServicesTests;

import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecRepositories.AccountRepository;
import com.example.Angle.Repositories.CommentRepository;
import com.example.Angle.Repositories.VideoRepository;
import com.example.Angle.Config.SecServices.Account.AccountAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountAdminServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private AccountAdminService accountAdminService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId("userId");
        account.setActive(true);
        account.setLikedVideos(new ArrayList<>());
        account.setDislikedVideos(new ArrayList<>());
    }

    @Test
    void testRemoveDislikeInteractions_NoDislikers() {
        String videoId = "videoId";
        when(accountRepository.findUsersWhoDislikeVideo(videoId)).thenReturn(new ArrayList<>());

        accountAdminService.removeDislikeInteractions(videoId);

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testRemoveDislikeInteractions_WithDislikers() {
        String videoId = "videoId";
        account.getDislikedVideos().add(videoId);
        List<Account> dislikingAccounts = List.of(account);
        when(accountRepository.findUsersWhoDislikeVideo(videoId)).thenReturn(dislikingAccounts);

        accountAdminService.removeDislikeInteractions(videoId);

        assertFalse(account.getDislikedVideos().contains(videoId));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testRemoveLikeInteractions_NoLikers() {
        String videoId = "videoId";
        when(accountRepository.findUsersWhoLikeVideo(videoId)).thenReturn(new ArrayList<>());

        accountAdminService.removeLikeInteractions(videoId);

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testRemoveLikeInteractions_WithLikers() {
        String videoId = "videoId";
        account.getLikedVideos().add(videoId);
        List<Account> likingAccounts = List.of(account);
        when(accountRepository.findUsersWhoLikeVideo(videoId)).thenReturn(likingAccounts);

        accountAdminService.removeLikeInteractions(videoId);

        assertFalse(account.getLikedVideos().contains(videoId));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testBanAccount_AccountExists() {
        String accountId = "userId";
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountAdminService.banAccount(accountId);

        assertFalse(account.isActive());
        verify(accountRepository, times(1)).save(account);
        verify(videoRepository, times(1)).banAllUserVideos(accountId);
        verify(commentRepository, times(1)).banAllUserComments(accountId);
    }

    @Test
    void testBanAccount_AccountNotExists() {
        String accountId = "nonExistentId";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> accountAdminService.banAccount(accountId));
    }

    @Test
    void testUnbanAccount_AccountExists() {
        String accountId = "userId";
        account.setActive(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountAdminService.unbanAccount(accountId);

        assertTrue(account.isActive());
        verify(accountRepository, times(1)).save(account);
        verify(videoRepository, times(1)).unbanAllUserVideos(accountId);
        verify(commentRepository, times(1)).unbanAllUserComments(accountId);
    }

    @Test
    void testUnbanAccount_AccountNotExists() {
        String accountId = "nonExistentId";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> accountAdminService.unbanAccount(accountId));
    }

    @Test
    void testAddUser() {
        accountAdminService.addUser(account);

        verify(accountRepository, times(1)).save(account);
    }
}
