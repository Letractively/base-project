package org.damour.base.server.hibernate.helpers;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.server.hibernate.IDefaultData;
import org.hibernate.Session;

import com.twmacinta.util.MD5;

public class DefaultData implements IDefaultData {

  public void create(Session session) {
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
      admin.setValidated(true);
      session.save(admin);

      UserGroup group = new UserGroup();
      group.setName("admin-group");
      group.setOwner(admin);
      session.save(group);

      GroupMembership membership = new GroupMembership();
      membership.setUser(admin);
      membership.setUserGroup(group);
      session.save(membership);

      File f = new File();
      f.setName("Test File");
      f.setOwner(admin);
      session.save(f);
    }
    User anonymous = UserHelper.getUser(session, "anonymous");
    if (anonymous == null) {
      anonymous = new User();
      anonymous.setUsername("anonymous");
      MD5 md5 = new MD5();
      md5.Update("s,!5C6xAwM");
      anonymous.setPasswordHash(md5.asHex());
      anonymous.setFirstname("anonymous");
      anonymous.setLastname("anonymous");
      anonymous.setPasswordHint("default");
      anonymous.setSignupDate(System.currentTimeMillis());
      anonymous.setAdministrator(false);
      anonymous.setValidated(true);
      session.save(anonymous);

      UserGroup group = new UserGroup();
      group.setName("anonymous-group");
      group.setOwner(anonymous);
      session.save(group);

      GroupMembership membership = new GroupMembership();
      membership.setUser(anonymous);
      membership.setUserGroup(group);
      session.save(membership);
    }
  }
}
