package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.dto.AdminDTO;
import com.avesta.mastercrawler.mapper.AdminMapper;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.service.IUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UsersTableRestController {

    private final IUsersService iUsersService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersTableRestController(IUsersService iUsersService, PasswordEncoder passwordEncoder) {
        this.iUsersService = iUsersService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/data")
    @ResponseBody
    public Map<String, Object> getPaginatedAdmins(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam("search[value]") String searchValue,
            @RequestParam("order[0][column]") int orderColumn,
            @RequestParam("order[0][dir]") String orderDir) {


        String[] columnMapping = {"userId", "email", "status", "createdAt", "updatedAt"};
        String orderBy = columnMapping[orderColumn];

        Pageable pageable = PageRequest.of(start / length, length,
                orderDir.equals("desc") ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());


        Page<Users> usersPage = iUsersService.findAllAdminsWithFilters(searchValue, pageable);

        List<Map<String, Object>> usersData = usersPage.getContent().stream().map(users -> {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", users.getUserId());
            data.put("email", users.getEmail());
            data.put("status", users.getActive());
            data.put("createdAt", users.getCreatedAt());
            data.put("updatedAt", users.getUpdatedAt());
            data.put("actions", "<div class='hstack gap-3 align-items-center'>" +
                    "<button class='btn btn-link text-success p-0 d-inline-block edit-user' data-user-id='" + users.getUserId() + "'>" +
                    "<i class='ri-pencil-fill fs-16'></i>" +
                    "</button>"+
                    "</div>");


            return data;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", usersPage.getTotalElements());
        response.put("recordsFiltered", usersPage.getTotalElements());
        response.put("data", usersData);

        return response;
    }

    @GetMapping("/admins/{id}")
    @ResponseBody
    public AdminDTO getAdminById(@PathVariable Integer id) {
        Optional<Users> user = iUsersService.findById(id);
        AdminDTO adminDTO = AdminMapper.toDTO(user.get());
        return adminDTO;
    }

    @PostMapping("/admins/edit/save")
    @ResponseBody
    public ResponseEntity<String> createOrUpdateAdmin(@RequestBody AdminDTO adminDTO) {
        try {
            Users user;
            if (adminDTO.getId() != null) {
                Optional<Users> optionalUser = iUsersService.findById(adminDTO.getId());
                if (optionalUser.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
                user = optionalUser.get();
            } else {
                user = new Users();
            }

            user.setEmail(adminDTO.getEmail());
            if (adminDTO.getPassword() != null && !adminDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(adminDTO.getPassword()));
            }

            user.setActive(adminDTO.isActive());

            iUsersService.savePassword(user);

            return ResponseEntity.ok("User saved successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save user");
        }
    }




}
