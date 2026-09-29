# Judging Integrity & Mathematics

The Judging Service is the core intellectual property of the Dogfood platform, designed to eliminate human bias from hackathon scoring.

## 1. Calibration Round
Before the actual judging begins, the platform enforces a **Calibration Round**.
* **Mechanics:** All judges are given the same set of 3 "dummy" or "baseline" projects to score. 
* **Purpose:** This establishes an initial baseline mean and standard deviation for every judge before they begin scoring actual submissions, ensuring our normalization algorithms have robust data from the very first real vote.

## 2. Z-Score Normalization
Raw scores (1-10) are meaningless because "Harsh Harry" might give an average of 4, while "Generous Gina" gives an average of 9.
Every score is normalized into a Z-Score:
`Z = (raw_score - judge_mean) / judge_std_dev`

### Worked Numeric Example
Consider 3 Judges and 3 Projects.
* **Judge A (Generous):** Scores = 8, 9, 10. Mean = 9, StdDev = 1.
* **Judge B (Harsh):** Scores = 2, 3, 4. Mean = 3, StdDev = 1.
* **Judge C (Neutral):** Scores = 4, 6, 8. Mean = 6, StdDev = 2.

Suppose Project X is scored 9 by Judge A, 4 by Judge B, and 8 by Judge C.
* Judge A's Z-Score for X: `(9 - 9) / 1 = 0`
* Judge B's Z-Score for X: `(4 - 3) / 1 = +1`
* Judge C's Z-Score for X: `(8 - 6) / 2 = +1`

Average Z-Score for Project X: `(0 + 1 + 1) / 3 = 0.67`.
Even though Judge A gave the highest raw score (9), their vote contributed a 0 Z-score because it was perfectly average for them. Judge B gave a low raw score (4), but because it was high for them, it contributed a +1 Z-score.

## 3. Bayesian Shrinkage & Normalization Proof
Judges who only review 1 or 2 projects have highly volatile statistics. A judge giving a single score of 10 might have a standard deviation of 0, breaking raw Z-score calculations. We apply **Bayesian Shrinkage** to pull their normalized scores toward the global mean:

$$\lambda = \frac{k}{k + k_0} \quad (k_0 = 5)$$

$$\bar{z}' = \lambda \cdot z + (1 - \lambda) \cdot \mu_{\text{global}}$$

### Mathematical Proof of Bias Neutralization:
1. **Hawk/Dove Invariance**:  
   Let Judge $A$ (dove) evaluate with mean $\mu_A = 8, \sigma_A = 1$ and Judge $B$ (hawk) evaluate with $\mu_B = 4, \sigma_B = 1$.  
   If both judges review a standout project at $+2\sigma$ above their subjective standard ($x_A = 10$, $x_B = 6$):
   $$z_A = \frac{10 - 8}{1} = +2.0, \quad z_B = \frac{6 - 4}{1} = +2.0$$
   Both evaluations yield identical normalized contributions ($+2.0$), rendering hawk/dove leniency mathematically irrelevant.

2. **Low-Volume Outlier Dampening ($k_0 = 5$)**:
   - For an uncalibrated judge with only $k = 1$ review: $\lambda = \frac{1}{1 + 5} = 0.167$. Their score is **dampened by 83.3%** toward the consensus mean, preventing single rogue reviews from determining rank.
   - For a calibrated judge with $k = 15$ reviews: $\lambda = \frac{15}{15 + 5} = 0.75$. Outlier dampening drops to only 25%, granting full weight to reliable reviewers.

3. **Zero Variance Edge Case ($s_{dev} = 0$)**:
   When a judge assigns identical scores to all submissions, $\sigma = 0$. The engine explicitly defaults $\sigma = 1.0$ and assigns $z = 0$, guaranteeing numerical stability without division-by-zero runtime exceptions. *(Verified in `NormalizationEngineTest.java`)*.

## 4. Conflict of Interest (COI)
Judges must explicitly declare conflicts (e.g., mentoring, financial ties) via the Judge Dashboard before scoring begins. The system physically prevents assigning a judge to a declared COI team.

## 5. Quadratic Voting (Public Phase)
For public "Community Choice" awards, standard voting is vulnerable to popularity contests.
We use Quadratic Voting: The cost of casting `N` votes for a single project is `N^2` credits. This forces voters to express the *intensity* of their preference rather than just breadth.
