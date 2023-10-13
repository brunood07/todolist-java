package br.com.brunood.todolist.task;

import br.com.brunood.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    ITaskRepository taskRepository;

    @PostMapping
    public ResponseEntity create(@RequestBody TaskModel data, HttpServletRequest request) {
        data.setIdUser((UUID) request.getAttribute("idUser"));
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(data.getStartAt()) || currentDate.isAfter(data.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio/término deve ser maior que a data atual");
        }

        if (data.getStartAt().isAfter(data.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio deve ser menor que a data de término");
        }
        var task = this.taskRepository.save(data);

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskModel>> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel data, @PathParam("/{taskId}") UUID taskId,  HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var task = this.taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        if (!task.getIdUser().equals(idUser) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("O usuário não tem permissão para alterar essa tarefa");
        }
        Utils.copyNonNullProperties(data, task);

        var updatedTask = this.taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }
}
