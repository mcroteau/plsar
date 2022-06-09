package `plsar-auth`.support

interface DbAccess {
    /**
     * Intended to return the user's password based
     * on the username
     *
     * @param user
     * @return returns hashed password
     */
    fun getPassword(user: String?): String?

    /**
     * Takes a username
     *
     * @param user
     * @return returns a unique set of role strings
     */
    fun getRoles(user: String?): Set<String?>?

    /**
     *
     * @param user
     * @return returns a unique set of user permissions
     * example permission user:maintenance:(id) (id)
     * replaced with actual id of user
     */
    fun getPermissions(user: String?): Set<String?>?
}