CREATE VIEW teacher_statistic_view
AS
SELECT tp.id,
       COALESCE(active_classrooms.active_classrooms, 0) AS active_classrooms,
       COALESCE(student_accounts.student_accounts, 0)   AS student_accounts,
       COALESCE(shared_projects.shared_projects, 0)     AS shared_projects,
       COALESCE(draft_projects.draft_projects, 0)       AS draft_projects
FROM teacher_profiles tp
         LEFT JOIN (SELECT res.teacher_id,
                           sum(res.cnt) AS active_classrooms
                    FROM (SELECT teacher_id, count(*) AS cnt
                          FROM classrooms
                          GROUP BY teacher_id

                          UNION ALL

                          SELECT teacher_id, count(*) AS cnt
                          FROM co_teachers
                          WHERE co_teacher_status = 'CO_TEACHER'
                          GROUP BY teacher_id) AS res
                    GROUP BY res.teacher_id) AS active_classrooms ON tp.id = active_classrooms.teacher_id

         LEFT JOIN (SELECT res.teacher_id,
                           sum(res.cnt) AS student_accounts
                    FROM (SELECT cr.teacher_id, count(*) AS cnt
                          FROM student_profiles AS spr
                                   JOIN classrooms AS cr
                                        ON spr.classroom_id = cr.id
                          GROUP BY cr.teacher_id

                          UNION ALL

                          SELECT ct.teacher_id, count(*)
                          FROM student_profiles as sp
                                   JOIN co_teachers AS ct
                                        ON sp.classroom_id = ct.classroom_id
                          WHERE ct.co_teacher_status = 'CO_TEACHER'
                          GROUP BY ct.teacher_id) AS res
                    GROUP BY res.teacher_id) AS student_accounts ON tp.id = student_accounts.teacher_id

         LEFT JOIN (SELECT res.teacher_id,
                           sum(res.cnt) AS shared_projects
                    FROM (SELECT cr.teacher_id, count(*) AS cnt
                          FROM classrooms AS cr
                                   JOIN project_classroom_share AS pcrs1
                                        ON cr.id = pcrs1.classroom_id
                                   JOIN projects AS pr1
                                        ON pcrs1.project_id = pr1.id
                          WHERE pr1.owner_id IN (SELECT id FROM student_profiles)
                          GROUP BY cr.teacher_id

                          UNION ALL

                          SELECT ct.teacher_id, count(*)
                          FROM co_teachers AS ct
                                   JOIN project_classroom_share AS pcrs2
                                        ON pcrs2.classroom_id = ct.classroom_id
                                   JOIN projects AS pr2
                                        ON pcrs2.project_id = pr2.id
                                   JOIN student_profiles AS sp
                                        ON sp.id = pr2.owner_id
                          WHERE ct.co_teacher_status = 'CO_TEACHER'
                          GROUP BY ct.teacher_id) AS res
                    GROUP BY res.teacher_id) AS shared_projects ON tp.id = shared_projects.teacher_id

         LEFT JOIN (SELECT res.teacher_id,
                           sum(res.cnt) AS draft_projects
                    FROM (SELECT cr.teacher_id, count(*) AS cnt
                          FROM classrooms AS cr

                                   JOIN student_profiles as sp1
                                        ON sp1.classroom_id = cr.id

                                   JOIN projects AS pr1
                                        ON pr1.owner_id = sp1.id

                          WHERE pr1.id NOT IN (SELECT project_id FROM project_classroom_share)
                          GROUP BY cr.teacher_id

                          UNION ALL

                          SELECT ct.teacher_id, count(*)
                          FROM co_teachers AS ct
                                   JOIN student_profiles AS sp2
                                        ON sp2.classroom_id = ct.classroom_id
                                   JOIN projects AS pr2
                                        ON pr2.owner_id = sp2.id
                          WHERE pr2.id NOT IN (SELECT project_id FROM project_classroom_share)
                            AND ct.co_teacher_status = 'CO_TEACHER'
                          GROUP BY ct.teacher_id) AS res
                    GROUP BY res.teacher_id) AS draft_projects ON tp.id = draft_projects.teacher_id;
