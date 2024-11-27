public class User {
    private final int id;
    private final String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("{\"id\":%d,\"name\":\"%s\"}", id, name);
    }
}