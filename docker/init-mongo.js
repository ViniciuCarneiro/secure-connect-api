db = db.getSiblingDB('secure_connect');

db.users.insert({
  _id: ObjectId("67a91b6e39fb645315d98a20"),
  name: "Usuario Admin",
  username: "admin",
  email: "admin@admin.com",
  password: "$2a$10$/mBXxUQ8lYbADSz.90J5GeNKz9V.Y4DLNvwvLarYA3hXODQEhgKWW",
  role: "ROLE_USER_ADMIN",
  _class: "com.secure.connect.secure_connect.user.domain.User",
  emailVerified: true,
  mfaEnabled: false
});

