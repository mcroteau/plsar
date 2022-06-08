package foo;

import shape.model.User;
import shape.repo.UserRepo;
import perched.support.access.Accessor;
import qio.annotate.Element;
import qio.annotate.Inject;

import java.util.Set;

@Element
public class AuthAccess implements DbAccess {

    @Inject
    UserRepo userRepo;

    public String getPassword(String phone){
        User user = userRepo.getPhone(phone);
        if(user != null){
            return user.getPassword();
        }
        return "";
    }

    public Set<String> getRoles(String phone){
        User user = userRepo.getPhone(phone);
        Set<String> roles = userRepo.getUserRoles(user.getId());
        return roles;
    }

    public Set<String> getPermissions(String phone){
        User user = userRepo.getPhone(phone);
        Set<String> permissions = userRepo.getUserPermissions(user.getId());
        return permissions;
    }

}
