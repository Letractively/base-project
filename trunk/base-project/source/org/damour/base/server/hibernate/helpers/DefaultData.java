package org.damour.base.server.hibernate.helpers;

import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.hibernate.Session;

import com.twmacinta.util.MD5;

public class DefaultData {

  public static void create(Session session) {
    User admin = UserHelper.getUser(session, "admin");
    if (admin == null) {
      admin = new User();
      admin.setUsername("admin");
      MD5 md5 = new MD5();
      md5.Update("p@$$w0rd");
      admin.setPasswordHash(md5.asHex());
      admin.setFirstname("Admin");
      admin.setLastname("Istrator");
      admin.setPasswordHint("default");
      admin.setEmail("admin@domain.com");
      admin.setSignupDate(System.currentTimeMillis());
      admin.setAdministrator(true);
      session.save(admin);

      UserGroup group = new UserGroup();
      group.setName("admin-group");
      group.setOwner(admin);
      session.save(group);

      GroupMembership membership = new GroupMembership();
      membership.setUser(admin);
      membership.setUserGroup(group);
      session.save(membership);
    }
  }
}
