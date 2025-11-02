INSERT INTO articles (id, slug, title, excerpt, image, read_time, published_on)
VALUES
    (uuid_generate_v4(), 'immersive-speaking-labs', 'Immersive speaking labs: what we learned after 6,000 sessions', 'How real-time feedback and simulated dialogues transformed learner confidence in under four weeks.', '/images/tinyframe.jpg', '6 min read', '2025-07-12'),
    (uuid_generate_v4(), 'curriculum-refresh', 'Refreshing curriculum with contract-first APIs', 'Move grammar modules from concept to classroom in days with contract-first versioning.', '/images/infoback.jpg', '4 min read', '2025-05-28'),
    (uuid_generate_v4(), 'data-driven-coaching', 'Data-driven coaching playbook for English cohorts', 'Use weekly analytics to prioritise 1:1 interventions and keep completion rates above 92%.', '/images/frame7.jpg', '8 min read', '2025-03-04');

WITH mapped AS (
    SELECT id, slug FROM articles WHERE slug IN ('immersive-speaking-labs', 'curriculum-refresh', 'data-driven-coaching')
)
INSERT INTO article_sections (id, article_id, order_index, content)
SELECT uuid_generate_v4(), mapped.id, payload.order_index, payload.content
FROM mapped
JOIN (
    VALUES
        ('immersive-speaking-labs', 0, 'We launched speaking labs to close the gap between theory and conversation. Two weeks later 78% of learners reported higher confidence and clearer pronunciation.'),
        ('immersive-speaking-labs', 1, 'The key was blending human coaching with AI prompts. Learners rotated through client calls, travel scenarios and support dialogues, each with instant phonetic feedback.'),
        ('immersive-speaking-labs', 2, 'Next we will expand multilingual support and tailor roleplays by industry so corporate cohorts can practise contextually relevant dialogues.'),
        ('curriculum-refresh', 0, 'Contract-first design lets curriculum authors and engineers collaborate asynchronously.'),
        ('curriculum-refresh', 1, 'Once the schema lands in Git the Kotlin stubs and TypeScript types regenerate automatically, so releases stay consistent across clients.'),
        ('data-driven-coaching', 0, 'We compared 12 cohorts that received data-driven nudges against a control group. Learners using EnglishMoon analytics completed more lessons and progressed faster.'),
        ('data-driven-coaching', 1, 'Coaches received Monday digests flagging learners who needed extra support. Automated reminders helped create a virtuous cycle of accountability.')
) AS payload(slug, order_index, content)
    ON payload.slug = mapped.slug;
