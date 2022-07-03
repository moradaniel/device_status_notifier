conn = new Mongo();
db = conn.getDB("devicestatusdb");
db.createUser(
    {
        user: "device_status_user",
        pwd: "local_pwd",
        roles: [
            {
                role: "readWrite",
                db: "devicestatusdb"
            }
        ]
    }
);