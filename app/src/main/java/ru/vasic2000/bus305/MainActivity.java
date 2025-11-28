package ru.vasic2000.bus305;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    private TextView tvStory, tvStop;
    private EditText etAnswer;
    private Button btnSubmit;
    private ImageView ivBusDoors;
    private LinearLayout layoutPeople;

    private int currentStop = 1;
    private List<String> passengers = new ArrayList<>(); // "M", "F", "C"
    private Random random = new Random();
    private Handler handler = new Handler();
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        startGame();

        btnSubmit.setOnClickListener(v -> checkAnswer());
    }

    private void initializeViews() {
        tvStory = findViewById(R.id.tvStory);
        tvStop = findViewById(R.id.tvStop);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivBusDoors = findViewById(R.id.ivBusDoors);
        layoutPeople = findViewById(R.id.layoutPeople);
    }

    private void startGame() {
        passengers.clear();
        currentStop = 1;
        score = 0;
        updateStopDisplay();

        showMessage("Автобус на остановке 1. Автобус пустой.");

        handler.postDelayed(() -> {
            openDoors();
            showMessage("Двери открываются...");
        }, 1000);

        handler.postDelayed(() -> {
            // Первая остановка - только взрослые заходят
            int peopleCount = random.nextInt(5) + 1;
            for (int i = 0; i < peopleCount; i++) {
                if (random.nextBoolean()) {
                    passengers.add("M");
                } else {
                    passengers.add("F");
                }
            }
            showPeople("Заходят:", passengers);
        }, 3000);

        handler.postDelayed(() -> {
            closeDoors();
            showMessage("Двери закрываются...");
        }, 6000);

        handler.postDelayed(this::nextStop, 8000);
    }

    private void nextStop() {
        currentStop++;
        updateStopDisplay();

        if (currentStop <= 5) {
            showStopAction();
        }
    }

    private void showStopAction() {
        showMessage("Остановка " + currentStop);

        handler.postDelayed(() -> {
            openDoors();
            showMessage("Двери открываются...");
        }, 1000);

        handler.postDelayed(() -> {
            // Люди выходят
            List<String> exiting = getExitingPeople();
            // Люди заходят
            List<String> entering = getEnteringPeople();

            // Обновляем пассажиров
            passengers.removeAll(exiting);
            passengers.addAll(entering);

            showPeopleMovement(exiting, entering);

        }, 3000);

        handler.postDelayed(() -> {
            closeDoors();
            showMessage("Двери закрываются...");
        }, 8000);

        handler.postDelayed(this::askQuestion, 10000);
    }

    private List<String> getExitingPeople() {
        List<String> exiting = new ArrayList<>();
        if (passengers.isEmpty()) return exiting;

        // От 0 до всех могут выйти, но на последних остановках можно выпускать больше
        int maxCanExit = Math.min(passengers.size(), currentStop);
        int exitCount = random.nextInt(maxCanExit + 1);

        for (int i = 0; i < exitCount; i++) {
            if (!passengers.isEmpty()) {
                int index = random.nextInt(passengers.size());
                exiting.add(passengers.get(index));
            }
        }
        return exiting;
    }

    private List<String> getEnteringPeople() {
        List<String> entering = new ArrayList<>();
        int peopleCount = random.nextInt(5) + 1;

        for (int i = 0; i < peopleCount; i++) {
            // После 3 остановки могут заходить дети
            if (currentStop > 3 && random.nextDouble() < 0.3) {
                entering.add("C"); // Ребенок
            } else {
                if (random.nextBoolean()) {
                    entering.add("M");
                } else {
                    entering.add("F");
                }
            }
        }
        return entering;
    }

    private void showPeopleMovement(List<String> exiting, List<String> entering) {
        layoutPeople.removeAllViews();
        StringBuilder story = new StringBuilder();

        if (!exiting.isEmpty()) {
            story.append("Выходят:\n");
            for (String person : exiting) {
                String display = getPersonDisplay(person);
                addPersonView(display, getColorForPerson(person));
                story.append(display).append("\n");
            }
        }

        if (!entering.isEmpty()) {
            story.append(entering.isEmpty() ? "" : "\n").append("Заходят:\n");
            for (String person : entering) {
                String display = getPersonDisplay(person);
                addPersonView(display, getColorForPerson(person));
                story.append(display).append("\n");
            }
        }

        tvStory.setText(story.toString());
    }

    private void showPeople(String title, List<String> people) {
        layoutPeople.removeAllViews();
        StringBuilder story = new StringBuilder(title + "\n");

        for (String person : people) {
            String display = getPersonDisplay(person);
            addPersonView(display, getColorForPerson(person));
            story.append(display).append("\n");
        }

        tvStory.setText(story.toString());
    }

    private String getPersonDisplay(String person) {
        switch (person) {
            case "M": return "👨 Мужчина";
            case "F": return "👩 Женщина";
            case "C": return "👶 Ребенок";
            default: return "";
        }
    }

    private int getColorForPerson(String person) {
        switch (person) {
            case "M": return R.color.male_color;
            case "F": return R.color.female_color;
            case "C": return R.color.child_color;
            default: return R.color.default_color;
        }
    }

    private void askQuestion() {
        if (currentStop >= 5) {
            // Последняя остановка - специальный вопрос
            askFinalQuestion();
        } else {
            askRegularQuestion();
        }
    }

    private void askRegularQuestion() {
        int questionType = random.nextInt(4);
        String question;
        int correctAnswer;

        switch (questionType) {
            case 0:
                question = "Сколько МУЖЧИН в автобусе?";
                correctAnswer = countPeople("M");
                break;
            case 1:
                question = "Сколько ЖЕНЩИН в автобусе?";
                correctAnswer = countPeople("F");
                break;
            case 2:
                question = "Сколько ДЕТЕЙ в автобусе?";
                correctAnswer = countPeople("C");
                break;
            default:
                question = "Сколько ВСЕГО человек в автобусе?";
                correctAnswer = passengers.size();
                break;
        }

        showQuestion(question, correctAnswer);
    }

    private void askFinalQuestion() {
        String question = "Сколько человек осталось в автобусе?";
        int correctAnswer = passengers.size();
        showQuestion(question, correctAnswer);
    }

    private void showQuestion(String question, int correctAnswer) {
        layoutPeople.removeAllViews();
        showMessage(question);
        etAnswer.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        etAnswer.setTag(correctAnswer);
    }

    private int countPeople(String type) {
        int count = 0;
        for (String person : passengers) {
            if (person.equals(type)) count++;
        }
        return count;
    }

    private void addPersonView(String text, int colorRes) {
        TextView personView = new TextView(this);
        personView.setText(text);
        personView.setTextSize(16);
        personView.setPadding(20, 10, 20, 10);
        personView.setBackgroundColor(ContextCompat.getColor(this, colorRes));
        personView.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 5, 0, 5);
        personView.setLayoutParams(params);

        layoutPeople.addView(personView);
    }

    private void showMessage(String message) {
        tvStory.setText(message);
    }

    private void updateStopDisplay() {
        tvStop.setText("Остановка: " + currentStop + "/5");
    }

    private void openDoors() {
        ivBusDoors.animate().scaleY(0.1f).setDuration(1000);
    }

    private void closeDoors() {
        ivBusDoors.animate().scaleY(1.0f).setDuration(1000);
    }

    private void checkAnswer() {
        try {
            int answer = Integer.parseInt(etAnswer.getText().toString());
            int correctAnswer = (int) etAnswer.getTag();

            if (answer == correctAnswer) {
                score++;
                if (currentStop >= 5) {
                    showVictory();
                } else {
                    showSuccess();
                    handler.postDelayed(this::nextStop, 2000);
                }
            } else {
                showGameOver();
            }

        } catch (NumberFormatException e) {
            etAnswer.setError("Введите число!");
        }
    }

    private void showSuccess() {
        etAnswer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        showMessage("✅ Правильно! Счет: " + score);
    }

    private void showVictory() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 Победа!")
                .setMessage("Вы выиграли!\nФинальный счет: " + score + "/5\n\nВсе пассажиры: " + getPassengerDetails())
                .setPositiveButton("Новая игра", (dialog, which) -> restartGame())
                .setCancelable(false)
                .show();
    }

    private void showGameOver() {
        new AlertDialog.Builder(this)
                .setTitle("❌ Конец игры")
                .setMessage("Неправильный ответ!\nВаш счет: " + score + "\n\nПравильный ответ: " + etAnswer.getTag() +
                        "\n" + getPassengerDetails())
                .setPositiveButton("Новая игра", (dialog, which) -> restartGame())
                .setCancelable(false)
                .show();
    }

    private String getPassengerDetails() {
        int men = countPeople("M");
        int women = countPeople("F");
        int children = countPeople("C");
        return "Мужчин: " + men + ", Женщин: " + women + ", Детей: " + children + ", Всего: " + passengers.size();
    }

    private void restartGame() {
        passengers.clear();
        etAnswer.setText("");
        etAnswer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        layoutPeople.removeAllViews();
        startGame();
    }
}