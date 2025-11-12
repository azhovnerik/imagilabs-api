CREATE INDEX IF NOT EXISTS idx_co_teachers_status_teacher
    ON co_teachers (co_teacher_status, teacher_id);

CREATE INDEX IF NOT EXISTS idx_co_teachers_status_classroom
    ON co_teachers (co_teacher_status, classroom_id);

CREATE INDEX IF NOT EXISTS idx_student_profiles_classroom
    ON student_profiles (classroom_id);

CREATE INDEX IF NOT EXISTS idx_projects_owner
    ON projects (owner_id);

CREATE INDEX IF NOT EXISTS idx_project_classroom_share_classroom
    ON project_classroom_share (classroom_id);

CREATE INDEX IF NOT EXISTS idx_classrooms_teacher
    ON classrooms (teacher_id);


DROP VIEW IF EXISTS teacher_statistic_view;

CREATE VIEW teacher_statistic_view AS
SELECT tp.id,
       COALESCE(active_classrooms.active_classrooms, 0) AS active_classrooms,
       COALESCE(student_accounts.student_accounts, 0)   AS student_accounts,
       COALESCE(shared_projects.shared_projects, 0)     AS shared_projects,
       COALESCE(draft_projects.draft_projects, 0)       AS draft_projects
FROM teacher_profiles tp

         LEFT JOIN (SELECT teacher_id, COUNT(*) AS active_classrooms
                    FROM (SELECT teacher_id
                          FROM classrooms

                          UNION ALL

                          SELECT teacher_id
                          FROM co_teachers
                          WHERE co_teacher_status = 'CO_TEACHER') t
                    GROUP BY teacher_id) active_classrooms ON tp.id = active_classrooms.teacher_id

         LEFT JOIN (SELECT teacher_id, COUNT(*) AS student_accounts
                    FROM (SELECT cr.teacher_id, sp.id
                          FROM student_profiles sp
                                   JOIN classrooms cr ON sp.classroom_id = cr.id

                          UNION ALL

                          SELECT ct.teacher_id, sp.id
                          FROM student_profiles sp
                                   JOIN co_teachers ct ON sp.classroom_id = ct.classroom_id
                          WHERE ct.co_teacher_status = 'CO_TEACHER') t
                    GROUP BY teacher_id) student_accounts ON tp.id = student_accounts.teacher_id

         LEFT JOIN (SELECT teacher_id, COUNT(DISTINCT project_id) AS shared_projects
                    FROM (SELECT cr.teacher_id, pcrs.project_id
                          FROM classrooms cr
                                   JOIN project_classroom_share pcrs ON cr.id = pcrs.classroom_id
                                   JOIN projects pr ON pcrs.project_id = pr.id
                          WHERE EXISTS (SELECT 1
                                        FROM student_profiles sp
                                        WHERE sp.id = pr.owner_id)

                          UNION ALL

                          SELECT ct.teacher_id, pcrs.project_id
                          FROM co_teachers ct
                                   JOIN project_classroom_share pcrs ON ct.classroom_id = pcrs.classroom_id
                                   JOIN projects pr ON pcrs.project_id = pr.id
                          WHERE ct.co_teacher_status = 'CO_TEACHER'
                            AND EXISTS (SELECT 1
                                        FROM student_profiles sp
                                        WHERE sp.id = pr.owner_id)) t
                    GROUP BY teacher_id) shared_projects ON tp.id = shared_projects.teacher_id

         LEFT JOIN (SELECT teacher_id, COUNT(DISTINCT project_id) AS draft_projects
                    FROM (SELECT cr.teacher_id, pr.id AS project_id
                          FROM classrooms cr
                                   JOIN student_profiles sp ON sp.classroom_id = cr.id
                                   JOIN projects pr ON pr.owner_id = sp.id
                          WHERE NOT EXISTS (SELECT 1
                                            FROM project_classroom_share pcs
                                            WHERE pcs.project_id = pr.id)

                          UNION ALL

                          SELECT ct.teacher_id, pr.id AS project_id
                          FROM co_teachers ct
                                   JOIN student_profiles sp ON sp.classroom_id = ct.classroom_id
                                   JOIN projects pr ON pr.owner_id = sp.id
                          WHERE ct.co_teacher_status = 'CO_TEACHER'
                            AND NOT EXISTS (SELECT 1
                                            FROM project_classroom_share pcs
                                            WHERE pcs.project_id = pr.id)) t
                    GROUP BY teacher_id) draft_projects ON tp.id = draft_projects.teacher_id;
