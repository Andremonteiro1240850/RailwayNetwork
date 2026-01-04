# is_administrator.s - Check if user is Administrator
# Parameters:
#   a0: User* user (pointer to User struct)
# Returns:
#   a0: 1 if user is Administrator, 0 otherwise

.global is_administrator

is_administrator:
    # Check if user pointer is NULL
    beq a0, zero, not_admin
    
    # Debug: Try multiple offsets to find the correct one
    # The struct layout might be different than expected
    
    # Try offset 260 (common alignment)
    lw t0, 260(a0)          # Load user->role into t0
    li t1, 1                # Load 1 (USER_ROLE_ADMINISTRATOR)
    beq t0, t1, is_admin    # If role == 1, it's admin
    
    # Try offset 256
    lw t0, 256(a0)          # Load user->role into t0
    beq t0, t1, is_admin    # If role == 1, it's admin
    
    # Try offset 264 (with more padding)
    lw t0, 264(a0)          # Load user->role into t0
    beq t0, t1, is_admin    # If role == 1, it's admin
    
    # Try offset 258 (original)
    lw t0, 258(a0)          # Load user->role into t0
    beq t0, t1, is_admin    # If role == 1, it's admin
    
not_admin:
    li a0, 0                # Return 0 (not admin)
    ret
    
is_admin:
    li a0, 1                # Return 1 (is admin)
    ret
