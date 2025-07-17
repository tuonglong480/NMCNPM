// Giả định có các lớp/phương thức riêng biệt cho TaskDataAccess, TaskValidator, v.v.

public class PersonalTaskManagerRefactored {

    // Dependencies (có thể được inject qua constructor để tuân thủ DIP nếu có cấu trúc lớn hơn)
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Các phương thức trợ giúp đã tách ra (chuyển sang một lớp riêng nếu phù hợp với SOLID)
    private static JSONArray loadTasksFromDb() { /* ... */ }
    private static void saveTasksToDb(JSONArray tasksData) { /* ... */ }

    // Phương thức Validation đã tách
    private static boolean isValidTaskData(String title, String dueDateStr, String priorityLevel) {
        // Centralized validation logic
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return false;
        }
        try {
            LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
            return false;
        }
        String[] validPriorities = {"Thấp", "Trung bình", "Cao"};
        boolean isValidPriority = false;
        for (String validP : validPriorities) {
            if (validP.equals(priorityLevel)) {
                isValidPriority = true;
                break;
            }
        }
        if (!isValidPriority) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Vui lòng chọn từ: Thấp, Trung bình, Cao.");
            return false;
        }
        return true;
    }

    // Phương thức kiểm tra trùng lặp đã tách
    private static boolean isDuplicateTask(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject existingTask = (JSONObject) obj;
            if (existingTask.get("title").toString().equalsIgnoreCase(title) &&
                existingTask.get("due_date").toString().equals(dueDateStr)) {
                System.out.println(String.format("Lỗi: Nhiệm vụ '%s' đã tồn tại với cùng ngày đến hạn.", title));
                return true;
            }
        }
        return false;
    }

    /**
     * Chức năng thêm nhiệm vụ mới (đã refactor)
     *
     * @param title Tiêu đề nhiệm vụ.
     * @param description Mô tả nhiệm vụ.
     * @param dueDateStr Ngày đến hạn (định dạng YYYY-MM-DD).
     * @param priorityLevel Mức độ ưu tiên ("Thấp", "Trung bình", "Cao").
     * @return JSONObject của nhiệm vụ đã thêm, hoặc null nếu có lỗi.
     */
    public JSONObject addNewTask(String title, String description,
                                 String dueDateStr, String priorityLevel) { // Removed isRecurring

        // Step 1: Validate input using a dedicated helper [KISS, SRP]
        if (!isValidTaskData(title, dueDateStr, priorityLevel)) {
            return null;
        }

        // Step 2: Load data [DRY]
        JSONArray tasks = loadTasksFromDb();

        // Step 3: Check for duplicates [DRY, SRP]
        if (isDuplicateTask(tasks, title, dueDateStr)) {
            return null;
        }

        // Step 4: Create new task object (simplified ID generation, removed YAGNI properties)
        // Using a simpler ID mechanism or acknowledging UUID if it's truly needed.
        String taskId = generateTaskId(); // Simplified ID generation, e.g., simple counter or UUID if justified
        JSONObject newTask = new JSONObject();
        newTask.put("id", taskId);
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDateStr);
        newTask.put("priority", priorityLevel);
        newTask.put("status", "Chưa hoàn thành");
        newTask.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("last_updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        // Removed 'is_recurring' and 'recurrence_pattern' [YAGNI]

        tasks.add(newTask);

        // Step 5: Save data [DRY]
        saveTasksToDb(tasks);

        System.out.println(String.format("Đã thêm nhiệm vụ mới thành công với ID: %s", taskId));
        return newTask;
    }

    // Example of a simpler ID generation (for YAGNI illustration)
    private static int nextId = 1;
    private synchronized String generateTaskId() {
        return String.valueOf(nextId++);
    }

    public static void main(String[] args) {
        PersonalTaskManagerRefactored manager = new PersonalTaskManagerRefactored();

        System.out.println("\nThêm nhiệm vụ hợp lệ:");
        manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
        );

        System.out.println("\nThêm nhiệm vụ trùng lặp:");
        manager.addNewTask(
                "Mua sách",
                "Sách Công nghệ phần mềm.",
                "2025-07-20",
                "Cao"
        );

        System.out.println("\nThêm nhiệm vụ với tiêu đề rỗng:");
        manager.addNewTask(
                "",
                "Nhiệm vụ không có tiêu đề.",
                "2025-07-22",
                "Thấp"
        );
    }
}
