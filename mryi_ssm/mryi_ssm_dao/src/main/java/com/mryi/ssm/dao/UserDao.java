package com.mryi.ssm.dao;

import com.mryi.ssm.Role;
import com.mryi.ssm.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserDao {
  @Select("select * from users where username=#{username}")
  @Results({
    @Result(id = true, property = "id", column = "id"),
    @Result(property = "username", column = "username"),
    @Result(property = "email", column = "email"),
    @Result(property = "password", column = "password"),
    @Result(property = "phoneNum", column = "phoneNum"),
    @Result(property = "status", column = "status"),
    @Result(
        property = "roles",
        column = "id",
        javaType = java.util.List.class,
        many = @Many(select = "com.mryi.ssm.dao.RoleDao.findRoleByUserId"))
  })
  public UserInfo findByUsername(String username) throws Exception;

  @Select("select * from users")
  public List<UserInfo> findAll() throws Exception;

  @Insert(
      "insert into users(email,username,password,phoneNum,status) values(#{email},#{username},#{password},#{phoneNum},#{status})")
  void save(UserInfo userInfo);

  @Select("select * from users where id=#{id}")
  @Results({
    @Result(id = true, property = "id", column = "id"),
    @Result(property = "username", column = "username"),
    @Result(property = "email", column = "email"),
    @Result(property = "password", column = "password"),
    @Result(property = "phoneNum", column = "phoneNum"),
    @Result(property = "status", column = "status"),
    @Result(
        property = "roles",
        column = "id",
        javaType = java.util.List.class,
        many = @Many(select = "com.mryi.ssm.dao.RoleDao.findRoleByUserId"))
  })
  UserInfo findById(String id);

  @Select(
      "select * from role where id not in (select roleId from users_role where userId=#{userId})")
  List<Role> findOtherRoles(String userId);

  @Insert("insert into users_role(userId,roleId) values(#{userId},#{roleId})")
  void addRoleToUser(@Param("userId") String userId, @Param("roleId") String roleId);
}
